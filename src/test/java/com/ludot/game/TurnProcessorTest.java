package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.PieceEffect;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.output.RecordingListener;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.random.Dice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.random.RandomMocks.coinLanding;
import static com.ludot.random.RandomMocks.diceRolling;
import static com.ludot.random.RandomMocks.pickerChoosing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TurnProcessorTest {

    private final RecordingListener listener = new RecordingListener();
    private final Board board = new Board();
    private final GameWiring wiring = new GameWiring(board, listener);
    private final PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
    private final Player red = playerFactory.createPlayer(Colour.RED);
    private final Player green = playerFactory.createPlayer(Colour.GREEN);

    @Test
    void sixGivesAnotherRoll() {
        turnsRolling(6, 2).playTurn(red);
        assertEquals(2, countRolls());
    }

    @Test
    void thirdSixIsIgnored() {
        Dice dice = diceRolling(6, 6, 6);
        turnsRolling(dice).playTurn(red);
        verify(dice, times(3)).roll();
    }

    @Test
    void twoSixesThenAnotherNumberEndTheTurnNormally() {
        turnsRolling(6, 6, 3).playTurn(red);
        assertEquals(3, countRolls());
    }

    @Test
    void sixEntersAPieceAndShowsStatus() {
        turnsRolling(6, 1).playTurn(red);
        assertTrue(listener.hasEvent("entered R1"));
        assertTrue(listener.hasEvent("status RED 1/3"));
    }

    @Test
    void noLegalMoveIgnoresTheThrow() {
        turnsRolling(4).playTurn(red);
        assertEquals(1, countRolls());
        assertTrue(red.pieces().stream().allMatch(Piece::isInBase));
    }

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

    @Test
    void twoThreesSendTheBriefedPieceToBase() {
        Piece red1 = red.pieces().getFirst();
        board.enter(red1, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Briefing());
        TurnProcessor turns = turnsRolling(3, 3);
        turns.playTurn(red);
        turns.playTurn(red);
        assertTrue(red1.isInBase());
        assertTrue(listener.hasEvent("briefing return R1"));
    }

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
        assertTrue(listener.hasEvent("blocked throw ignored RED"));
    }

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

    // Energised pieces move 12 on a normal six, so only the breakaway reaches green
    @Test
    void captureWhileBreakingABlockadeGivesNoBonusRoll() {
        Piece red1 = red.pieces().get(0);
        Piece red2 = red.pieces().get(1);
        Piece red3 = red.pieces().get(2);
        Piece green1 = green.pieces().getFirst();
        for (Piece piece : List.of(red1, red2)) {
            board.enter(piece, Direction.CLOCKWISE);
            board.move(piece, Position.onTrack(30));
            piece.applyEffect(new PieceEffect.Energised());
        }
        board.enter(red3, Direction.CLOCKWISE);
        board.move(red3, Position.onTrack(10));
        board.move(red.pieces().get(3), Position.home());
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(36));
        turnsRolling(6, 6, 6, 1).playTurn(red);
        assertTrue(green1.isInBase());
        assertEquals(3, countRolls());
    }

    private TurnProcessor turnsRolling(Integer... rolls) {
        return turnsRolling(diceRolling(rolls));
    }

    private TurnProcessor turnsRolling(Dice dice) {
        return wiring.turnProcessor(dice, coinLanding(true), wiring.mysteryCells(pickerChoosing(0)));
    }

    private int countRolls() {
        return (int) listener.events().stream().filter(event -> event.startsWith("rolled")).count();
    }
}