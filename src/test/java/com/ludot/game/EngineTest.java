package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.PieceEffect;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.movement.CommandFactory;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.mystery.Teleporter;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.random.CellPicker;
import com.ludot.random.Coin;
import com.ludot.random.Dice;
import com.ludot.random.SeededRandomness;
import com.ludot.rules.MovePlanner;
import com.ludot.testsupport.TestDoubles.RecordingObserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.ludot.testsupport.TestDoubles.fixedCoin;
import static com.ludot.testsupport.TestDoubles.preferring;
import static com.ludot.testsupport.TestDoubles.scriptedDice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineTest {

    private static final List<Colour> TURN_ORDER = List.of(Colour.RED, Colour.GREEN, Colour.YELLOW, Colour.BLUE);

    private final RecordingObserver observer = new RecordingObserver();
    private final Board board = new Board();
    private final PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
    private final List<Player> players = Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();
    private final Player red = players.stream()
            .filter(player -> player.colour() == Colour.RED).findFirst().orElseThrow();

    @Nested
    class TurnOrder {

        @Test
        @DisplayName("Brief 3.1: the highest opening roll starts, then play goes clockwise")
        void highestRollerStartsAndOrderIsClockwise() {
            TurnOrderResolver resolver = new TurnOrderResolver(scriptedDice(2, 3, 6, 1), observer);
            assertEquals(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN), resolver.resolve(TURN_ORDER));
            assertTrue(observer.hasEvent("opening roll YELLOW 6"));
        }

        @Test
        @DisplayName("A3: only tied players roll again")
        void tiedPlayersRollAgain() {
            TurnOrderResolver resolver = new TurnOrderResolver(scriptedDice(6, 6, 1, 2, 3, 5), observer);
            assertEquals(Colour.GREEN, resolver.resolve(TURN_ORDER).get(0));
        }
    }

    @Nested
    class Turns {

        @Test
        @DisplayName("R4: a six gives another roll, any other number ends the turn")
        void sixGivesAnotherRoll() {
            turnsRolling(6, 2).playTurn(red);
            assertEquals(2, countRolls());
        }

        @Test
        @DisplayName("R4: the third six in a row is ignored and the turn ends")
        void thirdSixIsIgnored() {
            turnsRolling(6, 6, 6).playTurn(red);
            assertTrue(observer.hasEvent("roll ignored RED"));
            assertEquals(3, countRolls());
        }

        @Test
        @DisplayName("R2: a six moves a piece from base to X and the new status is shown")
        void sixEntersAPieceAndShowsStatus() {
            turnsRolling(6, 1).playTurn(red);
            assertTrue(observer.hasEvent("entered R1"));
            assertTrue(observer.hasEvent("status RED 1/3"));
        }

        @Test
        @DisplayName("R2: with every piece in base and no six, the throw is ignored")
        void noLegalMoveIgnoresTheThrow() {
            turnsRolling(4).playTurn(red);
            assertTrue(observer.hasEvent("no move RED"));
        }

        @Test
        @DisplayName("T-6: breaking a blockade moves every piece but one")
        void breakingABlockadeMovesAllButOnePiece() {
            Piece red1 = red.pieces().get(0);
            Piece red2 = red.pieces().get(1);
            board.enter(red1, Direction.CLOCKWISE);
            board.enter(red2, Direction.CLOCKWISE);
            rollResolver(scriptedDice(), fixedCoin(true), preferring(0)).breakBlockades(red);
            assertEquals(Position.onTrack(26), red1.position());
            assertEquals(Position.onTrack(32), red2.position());
        }

        @Test
        @DisplayName("T-13 + A2: two threes in a row send a briefed piece to base during a game")
        void twoThreesSendTheBriefedPieceToBase() {
            Piece red1 = red.pieces().get(0);
            board.enter(red1, Direction.CLOCKWISE);
            red1.applyEffect(new PieceEffect.Briefing());
            TurnProcessor turns = turnsRolling(3, 3);
            turns.playTurn(red);
            turns.playTurn(red);
            assertTrue(red1.isInBase());
            assertTrue(observer.hasEvent("briefing return R1"));
        }
    }

    @Nested
    class WholeGame {

        @Test
        @DisplayName("R11: a full seeded game ranks all four players and announces the winner")
        void fullGameRanksAllFourPlayers() {
            GameResultDto result = seededEngine().run(TURN_ORDER);
            assertEquals(4, result.finishingOrder().size());
            assertFalse(result.stalled());
            assertTrue(observer.hasEvent("finished " + result.finishingOrder().get(0) + " 1"));
            assertTrue(observer.hasEvent("game over " + result.finishingOrder()));
        }

        @Test
        @DisplayName("A8: a game in which no piece can ever move stops after 50 unchanged rounds")
        void gameWithoutProgressIsStalled() {
            Dice alwaysOne = () -> 1;
            MysteryCellManager mysteryCells = new MysteryCellManager(board, preferring(0), observer);
            TurnProcessor turns = turnProcessor(alwaysOne, fixedCoin(true), mysteryCells);
            GameResultDto result = new GameEngine(players, turns, mysteryCells, observer).run(TURN_ORDER);
            assertTrue(result.stalled());
            assertEquals(4, result.finishingOrder().size());
            assertTrue(observer.hasEvent("stalled " + result.rounds()));
        }
    }

    private TurnProcessor turnsRolling(int... rolls) {
        return turnProcessor(scriptedDice(rolls), fixedCoin(true), preferring(0));
    }

    private GameEngine seededEngine() {
        SeededRandomness random = new SeededRandomness(1);
        MysteryCellManager mysteryCells = new MysteryCellManager(board, random, observer);
        TurnProcessor turns = turnProcessor(random, random, mysteryCells);
        return new GameEngine(players, turns, mysteryCells, observer);
    }

    private TurnProcessor turnProcessor(Dice dice, Coin coin, CellPicker cellPicker) {
        return turnProcessor(dice, coin, new MysteryCellManager(board, cellPicker, observer));
    }

    private TurnProcessor turnProcessor(Dice dice, Coin coin, MysteryCellManager mysteryCells) {
        return new TurnProcessor(dice, board, rollResolver(dice, coin, mysteryCells), observer);
    }

    private RollResolver rollResolver(Dice dice, Coin coin, CellPicker cellPicker) {
        return rollResolver(dice, coin, new MysteryCellManager(board, cellPicker, observer));
    }

    private RollResolver rollResolver(Dice dice, Coin coin, MysteryCellManager mysteryCells) {
        TrackNavigator navigator = new TrackNavigator();
        Teleporter teleporter = new Teleporter(board, dice, coin, navigator, observer);
        CommandFactory commands = new CommandFactory(board, coin, teleporter, observer);
        return new RollResolver(new MovePlanner(board, navigator), commands, mysteryCells);
    }

    private int countRolls() {
        return (int) observer.events().stream().filter(event -> event.startsWith("rolled")).count();
    }
}
