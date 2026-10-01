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
import com.ludot.output.RecordingObserver;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.random.CellPicker;
import com.ludot.random.Coin;
import com.ludot.random.Dice;
import com.ludot.random.SeededRandomness;
import com.ludot.rules.MovePlanner;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EngineTest {

    private static final int FIRST_ROUND = 1;
    private static final int UNCHANGED_ROUNDS_BEFORE_STALL = 50;
    private static final List<Colour> TURN_ORDER = List.of(Colour.RED, Colour.GREEN, Colour.YELLOW, Colour.BLUE);

    private final RecordingObserver observer = new RecordingObserver();
    private final Board board = new Board();
    private final PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
    private final List<Player> players = Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();
    private final Player red = playerOf(Colour.RED);
    private final Player green = playerOf(Colour.GREEN);

    // Brief 3.1: the highest opening roll starts, then play goes clockwise
    @Test
    void highestRollerStartsAndOrderIsClockwise() {
        TurnOrderResolver resolver = new TurnOrderResolver(diceRolling(2, 3, 6, 1), observer);
        assertEquals(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN), resolver.resolve(TURN_ORDER));
        assertTrue(observer.hasEvent("opening roll YELLOW 6"));
    }

    // A3: only tied players roll again
    @Test
    void tiedPlayersRollAgain() {
        Dice dice = diceRolling(6, 6, 1, 2, 3, 5);
        TurnOrderResolver resolver = new TurnOrderResolver(dice, observer);
        assertEquals(Colour.GREEN, resolver.resolve(TURN_ORDER).get(0));
        verify(dice, times(6)).roll();
    }

    // R4: a six gives another roll, any other number ends the turn
    @Test
    void sixGivesAnotherRoll() {
        turnsRolling(6, 2).playTurn(red);
        assertEquals(2, countRolls());
    }

    // R4: the third six in a row is ignored and the turn ends
    @Test
    void thirdSixIsIgnored() {
        Dice dice = diceRolling(6, 6, 6);
        turnProcessor(dice, coinLanding(true), pickerChoosing(0)).playTurn(red);
        assertTrue(observer.hasEvent("roll ignored RED"));
        verify(dice, times(3)).roll();
    }

    // R2: a six moves a piece from base to X and the new status is shown
    @Test
    void sixEntersAPieceAndShowsStatus() {
        turnsRolling(6, 1).playTurn(red);
        assertTrue(observer.hasEvent("entered R1"));
        assertTrue(observer.hasEvent("status RED 1/3"));
    }

    // R2: with every piece in base and no six, the throw is ignored
    @Test
    void noLegalMoveIgnoresTheThrow() {
        turnsRolling(4).playTurn(red);
        assertTrue(observer.hasEvent("no move RED"));
    }

    // T-6: every piece of a blockade but one must break away
    @Test
    void allPiecesButOneBreakAwayFromABlockade() {
        Piece red2 = red.pieces().get(1);
        Piece red3 = red.pieces().get(2);
        board.enter(red.pieces().get(0), Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        board.enter(red3, Direction.CLOCKWISE);
        assertEquals(List.of(red2, red3), anyRollResolver().piecesToBreakAway(red));
    }

    // T-6: a breakaway piece moves six cells in its own direction
    @Test
    void breakawayPieceMovesSixInItsOwnDirection() {
        Piece red2 = red.pieces().get(1);
        board.enter(red.pieces().get(0), Direction.CLOCKWISE);
        board.enter(red2, Direction.COUNTER_CLOCKWISE);
        anyRollResolver().breakAwayCommand(red2).orElseThrow().execute();
        assertEquals(Position.onTrack(20), red2.position());
    }

    // T-6 + T-3: a breakaway piece blocked on the way stops in front of the block instead of staying
    @Test
    void breakawayPieceStopsInFrontOfAnOpponentBlock() {
        Piece red2 = red.pieces().get(1);
        board.enter(red.pieces().get(0), Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        Piece green1 = green.pieces().get(0);
        Piece green2 = green.pieces().get(1);
        board.enter(green1, Direction.CLOCKWISE);
        board.enter(green2, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(30));
        board.move(green2, Position.onTrack(30));
        anyRollResolver().breakAwayCommand(red2).orElseThrow().execute();
        assertEquals(Position.onTrack(29), red2.position());
    }

    // T-13 + A2: two threes in a row send a briefed piece to base during a game
    @Test
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

    // R11: a full seeded game ranks all four players and announces the winner
    @Test
    void fullGameRanksAllFourPlayers() {
        GameResultDto result = seededEngine().run(TURN_ORDER);
        assertEquals(4, result.finishingOrder().size());
        assertFalse(result.stalled());
        assertTrue(observer.hasEvent("finished " + result.finishingOrder().get(0) + " 1"));
        assertTrue(observer.hasEvent("game over " + result.finishingOrder()));
    }

    // A8: a game in which no piece can ever move stops after 50 unchanged rounds
    @Test
    void gameWithoutProgressIsStalled() {
        Dice alwaysOne = diceRolling(1);
        MysteryCellManager mysteryCells = new MysteryCellManager(board, pickerChoosing(0), observer);
        TurnProcessor turns = turnProcessor(alwaysOne, coinLanding(true), mysteryCells);
        GameResultDto result = new GameEngine(players, turns, mysteryCells, observer).run(TURN_ORDER);
        assertTrue(result.stalled());
        assertEquals(4, result.finishingOrder().size());
        assertEquals(FIRST_ROUND + UNCHANGED_ROUNDS_BEFORE_STALL, result.rounds());
        assertTrue(observer.hasEvent("stalled " + result.rounds()));
    }

    // A3: a three-way tie re-rolls among the tied players only
    @Test
    void threeWayTieRollsAgain() {
        Dice dice = diceRolling(6, 6, 6, 2, 3, 5, 1);
        TurnOrderResolver resolver = new TurnOrderResolver(dice, observer);
        assertEquals(List.of(Colour.GREEN, Colour.YELLOW, Colour.BLUE, Colour.RED), resolver.resolve(TURN_ORDER));
        verify(dice, times(7)).roll();
    }

    // Brief 3.1: the round wraps round clockwise from whoever starts
    @Test
    void turnOrderWrapsRoundFromTheStarter() {
        TurnOrderResolver resolver = new TurnOrderResolver(diceRolling(1, 2, 3, 6), observer);
        assertEquals(List.of(Colour.BLUE, Colour.RED, Colour.GREEN, Colour.YELLOW), resolver.resolve(TURN_ORDER));
    }

    // R6 + T-2: a capture gives the player another roll
    @Test
    void captureGivesAnotherRoll() {
        Piece red1 = red.pieces().get(0);
        Piece green1 = green.pieces().get(0);
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(30));
        turnsRolling(4, 2).playTurn(red);
        assertTrue(observer.hasEvent("capture R1 G1"));
        assertTrue(green1.isInBase());
        assertEquals(2, countRolls());
    }

    private TurnProcessor turnsRolling(Integer... rolls) {
        return turnProcessor(diceRolling(rolls), coinLanding(true), pickerChoosing(0));
    }

    private RollResolver anyRollResolver() {
        return rollResolver(diceRolling(1), coinLanding(true), pickerChoosing(0));
    }

    private static Dice diceRolling(Integer... rolls) {
        Dice dice = mock(Dice.class);
        when(dice.roll()).thenReturn(rolls[0], Arrays.copyOfRange(rolls, 1, rolls.length));
        return dice;
    }

    private static Coin coinLanding(boolean heads) {
        Coin coin = mock(Coin.class);
        when(coin.tossHeads()).thenReturn(heads);
        return coin;
    }

    private static CellPicker pickerChoosing(int cell) {
        CellPicker cellPicker = mock(CellPicker.class);
        when(cellPicker.pick(anyList())).thenReturn(cell);
        return cellPicker;
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

    private Player playerOf(Colour colour) {
        return players.stream().filter(player -> player.colour() == colour).findFirst().orElseThrow();
    }

    private int countRolls() {
        return (int) observer.events().stream().filter(event -> event.startsWith("rolled")).count();
    }
}
