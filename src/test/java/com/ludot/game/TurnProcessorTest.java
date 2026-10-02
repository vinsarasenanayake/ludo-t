package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.PieceEffect;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.output.RecordingObserver;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.random.Dice;

import org.junit.jupiter.api.Test;

import static com.ludot.random.RandomMocks.coinLanding;
import static com.ludot.random.RandomMocks.diceRolling;
import static com.ludot.random.RandomMocks.pickerChoosing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TurnProcessorTest {

    private final RecordingObserver observer = new RecordingObserver();
    private final Board board = new Board();
    private final GameWiring wiring = new GameWiring(board, observer);
    private final PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
    private final Player red = playerFactory.createPlayer(Colour.RED);
    private final Player green = playerFactory.createPlayer(Colour.GREEN);

    // R4: a six rolls again
    @Test
    void sixGivesAnotherRoll() {
        turnsRolling(6, 2).playTurn(red);
        assertEquals(2, countRolls());
    }

    // R4: the third six ends the turn
    @Test
    void thirdSixIsIgnored() {
        Dice dice = diceRolling(6, 6, 6);
        turnsRolling(dice).playTurn(red);
        verify(dice, times(3)).roll();
    }

    // R4: two sixes then another number is normal
    @Test
    void twoSixesThenAnotherNumberEndTheTurnNormally() {
        turnsRolling(6, 6, 3).playTurn(red);
        assertEquals(3, countRolls());
    }

    // R2: a six enters and shows status
    @Test
    void sixEntersAPieceAndShowsStatus() {
        turnsRolling(6, 1).playTurn(red);
        assertTrue(observer.hasEvent("entered R1"));
        assertTrue(observer.hasEvent("status RED 1/3"));
    }

    // R2: no six, throw ignored
    @Test
    void noLegalMoveIgnoresTheThrow() {
        turnsRolling(4).playTurn(red);
        assertEquals(1, countRolls());
        assertTrue(red.pieces().stream().allMatch(Piece::isInBase));
    }

    // R6 + T-2: a capture rolls again
    @Test
    void captureGivesAnotherRoll() {
        Piece red1 = red.pieces().getFirst();
        Piece green1 = green.pieces().getFirst();
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(30));
        turnsRolling(4, 2).playTurn(red);
        assertTrue(green1.isInBase());
        assertEquals(2, countRolls());
    }

    // T-13: two threes across turns return it to base
    @Test
    void twoThreesSendTheBriefedPieceToBase() {
        Piece red1 = red.pieces().getFirst();
        board.enter(red1, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Briefing());
        TurnProcessor turns = turnsRolling(3, 3);
        turns.playTurn(red);
        turns.playTurn(red);
        assertTrue(red1.isInBase());
        assertTrue(observer.hasEvent("briefing return R1"));
    }

    // R4 + R7: an unusable six ends the turn
    @Test
    void unusableSixEndsTheTurn() {
        Piece green1 = green.pieces().get(0);
        Piece green2 = green.pieces().get(1);
        board.enter(green1, Direction.CLOCKWISE);
        board.enter(green2, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(26));
        board.move(green2, Position.onTrack(26));
        turnsRolling(6, 1).playTurn(red);
        assertEquals(1, countRolls());
        assertTrue(observer.hasEvent("blocked throw ignored RED"));
    }

    // R4 + T-6: the third six breaks the blockade
    @Test
    void thirdSixBreaksTheBlockade() {
        Piece red1 = red.pieces().get(0);
        Piece red2 = red.pieces().get(1);
        Piece red3 = red.pieces().get(2);
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        board.move(red1, Position.onTrack(30));
        board.move(red2, Position.onTrack(30));
        board.enter(red3, Direction.CLOCKWISE);
        board.move(red3, Position.onTrack(10));
        board.move(red.pieces().get(3), Position.home());
        turnsRolling(6, 6, 6).playTurn(red);
        assertEquals(Position.onTrack(30), red1.position());
        assertEquals(Position.onTrack(36), red2.position());
    }

    private TurnProcessor turnsRolling(Integer... rolls) {
        return turnsRolling(diceRolling(rolls));
    }

    private TurnProcessor turnsRolling(Dice dice) {
        return wiring.turnProcessor(dice, coinLanding(true), wiring.mysteryCells(pickerChoosing(0)));
    }

    private int countRolls() {
        return (int) observer.events().stream().filter(event -> event.startsWith("rolled")).count();
    }
}