package com.ludot.engine;

import com.ludot.command.CommandFactory;
import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.PieceEffect;
import com.ludot.domain.Position;
import com.ludot.dto.GameResultDto;
import com.ludot.infrastructure.SeededRandomness;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.port.CellPicker;
import com.ludot.port.Coin;
import com.ludot.port.Dice;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.MysteryCellManager;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;
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
    private final Player red = players.get(0);

    @Nested
    class TurnOrder {

        @Test
        @DisplayName("The highest opening roll starts, then play goes clockwise")
        void highestRollerStartsAndOrderIsClockwise() {
            TurnOrderResolver resolver = new TurnOrderResolver(scriptedDice(2, 3, 6, 1), observer);
            assertEquals(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN), resolver.resolve(TURN_ORDER));
        }

        @Test
        @DisplayName("Assumption A14: only tied players roll again")
        void tiedPlayersRollAgain() {
            TurnOrderResolver resolver = new TurnOrderResolver(scriptedDice(6, 6, 1, 2, 3, 5), observer);
            assertEquals(Colour.GREEN, resolver.resolve(TURN_ORDER).get(0));
        }

        @Test
        void everyOpeningRollIsReported() {
            new TurnOrderResolver(scriptedDice(2, 3, 6, 1), observer).resolve(TURN_ORDER);
            assertTrue(observer.hasEvent("opening roll YELLOW 6"));
        }
    }

    @Nested
    class Turns {

        @Test
        void nonSixGivesOneRollOnly() {
            turnsRolling(3).playTurn(red);
            assertEquals(1, countRolls());
        }

        @Test
        @DisplayName("Rule 4: a six gives another roll")
        void sixGivesAnotherRoll() {
            turnsRolling(6, 2).playTurn(red);
            assertEquals(2, countRolls());
        }

        @Test
        @DisplayName("Rule 4: the third six in a row is ignored and the turn ends")
        void thirdSixIsIgnored() {
            turnsRolling(6, 6, 6).playTurn(red);
            assertTrue(observer.hasEvent("roll ignored RED"));
            assertEquals(3, countRolls());
        }

        @Test
        @DisplayName("Rule 2: a six moves a piece from base to X")
        void chosenMoveIsExecuted() {
            turnsRolling(6, 1).playTurn(red);
            assertTrue(observer.hasEvent("entered R1"));
        }

        @Test
        void enteringThePieceReportsTheNewStatus() {
            turnsRolling(6, 1).playTurn(red);
            assertTrue(observer.hasEvent("status RED 1/3"));
        }

        @Test
        @DisplayName("Rule 7: with no legal move the throw is ignored")
        void noLegalMoveIgnoresTheThrow() {
            turnsRolling(4).playTurn(red);
            assertTrue(observer.hasEvent("no move RED"));
        }

        @Test
        @DisplayName("Rule T-6: breaking a blockade moves every piece but one")
        void breakingABlockadeMovesAllButOnePiece() {
            Piece red1 = red.pieces().get(0);
            Piece red2 = red.pieces().get(1);
            board.enter(red1, Direction.CLOCKWISE);
            board.enter(red2, Direction.CLOCKWISE);
            turnsRolling().breakBlockades(red);
            assertEquals(Position.onTrack(26), red1.position());
            assertEquals(Position.onTrack(32), red2.position());
        }

        @Test
        @DisplayName("Rule T-13: two threes in a row send a briefed piece to base")
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

        @Test
        void otherRollsLeaveTheBriefedPieceWhereItIs() {
            Piece red1 = red.pieces().get(0);
            board.enter(red1, Direction.CLOCKWISE);
            red1.applyEffect(new PieceEffect.Briefing());
            TurnProcessor turns = turnsRolling(3, 4);
            turns.playTurn(red);
            turns.playTurn(red);
            assertFalse(red1.isInBase());
        }
    }

    @Nested
    class WholeGame {

        @Test
        void everyPlayerIsIntroduced() {
            seededEngine().introducePlayers();
            assertTrue(observer.hasEvent("introduced BLUE"));
        }

        @Test
        @DisplayName("Rule 11: a full seeded game finishes with all four players ranked")
        void fullGameRanksAllFourPlayers() {
            GameResultDto result = seededEngine().run(TURN_ORDER);
            assertEquals(4, result.finishingOrder().size());
            assertFalse(result.stalled());
        }

        @Test
        @DisplayName("Rule 11: the first player to bring all pieces home wins")
        void winnerIsReported() {
            GameResultDto result = seededEngine().run(TURN_ORDER);
            assertTrue(observer.hasEvent("finished " + result.finishingOrder().get(0) + " 1"));
        }

        @Test
        @DisplayName("Rule T-10: a mystery cell appears during a game")
        void mysteryCellAppearsDuringAGame() {
            seededEngine().run(TURN_ORDER);
            assertTrue(observer.events().stream().anyMatch(event -> event.startsWith("mystery spawned")));
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
        TrackNavigator navigator = new TrackNavigator();
        Teleporter teleporter = new Teleporter(board, dice, coin, navigator, observer);
        CommandFactory commands = new CommandFactory(board, coin, navigator, teleporter, observer);
        return new TurnProcessor(dice, board, new MovePlanner(board, navigator), commands, mysteryCells, observer);
    }

    private int countRolls() {
        return (int) observer.events().stream().filter(event -> event.startsWith("rolled")).count();
    }
}
