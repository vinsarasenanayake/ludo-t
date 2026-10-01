package com.ludot.mystery;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.output.RecordingObserver;
import com.ludot.random.Coin;
import com.ludot.random.Dice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeleporterTest {

    private static final int ALPHA_FACE = 1;
    private static final int BETA_FACE = 2;
    private static final int GAMMA_FACE = 3;
    private static final int BASE_FACE = 4;
    private static final int START_FACE = 5;
    private static final int APPROACH_FACE = 6;

    private final Dice dice = mock(Dice.class);
    private final Coin coin = mock(Coin.class);
    private Board board;
    private RecordingObserver observer;
    private Piece red1;

    @BeforeEach
    void setUp() {
        board = new Board();
        observer = new RecordingObserver();
        red1 = new Piece(Colour.RED, 1);
        board.enter(red1, Direction.CLOCKWISE);
        board.move(red1, Position.onTrack(30));
    }

    // T-12: Alpha energises the piece on heads and makes it sick on tails
    @Test
    void alphaEffectDependsOnTheCoin() {
        Piece red2 = new Piece(Colour.RED, 2);
        board.enter(red2, Direction.CLOCKWISE);
        teleporterRolling(ALPHA_FACE, true).teleport(red1);
        teleporterRolling(ALPHA_FACE, false).teleport(red2);
        assertEquals(Position.onTrack(7), red1.position());
        assertEquals(10, red1.adjustRoll(5));
        assertEquals(2, red2.adjustRoll(5));
    }

    // T-13: Beta sends the piece to a briefing, and the teleport is reported
    @Test
    void betaSendsToBriefing() {
        teleporterRolling(BETA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
        assertTrue(observer.hasEvent("teleport R1 BETA"));
    }

    // T-14: Gamma turns a clockwise piece counter-clockwise
    @Test
    void gammaReversesAClockwisePiece() {
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(44), red1.position());
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    // T-14: Gamma sends a counter-clockwise piece on to Beta
    @Test
    void gammaSendsACounterClockwisePieceToBeta() {
        red1.reverseDirection();
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
        assertTrue(observer.hasEvent("effect R1 SENT_TO_BETA"));
        assertFalse(observer.hasEvent("teleport R1 BETA"));
    }

    // A6: teleporting to the approach counts as passing it
    @Test
    void approachSendsThePieceToItsApproachCell() {
        teleporterRolling(APPROACH_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(24), red1.position());
        assertEquals(1, red1.approachPasses());
    }

    // T-11 + T-9: Base sends the piece back to base and clears its information
    @Test
    void baseSendsThePieceToBase() {
        red1.recordCapture();
        teleporterRolling(BASE_FACE, true).teleport(red1);
        assertTrue(red1.isInBase());
        assertFalse(red1.hasCaptured());
        assertTrue(board.occupantsAt(30).isEmpty());
    }

    // T-11: X sends the piece back to its own start cell
    @Test
    void startSendsThePieceToItsX() {
        teleporterRolling(START_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(26), red1.position());
    }

    // T-11 + T-12: an Alpha teleport is reported with its effect
    @Test
    void alphaTeleportIsReported() {
        teleporterRolling(ALPHA_FACE, true).teleport(red1);
        assertTrue(observer.hasEvent("teleport R1 ALPHA"));
        assertTrue(observer.hasEvent("effect R1 ENERGISED"));
        verify(dice).roll();
        verify(coin).tossHeads();
    }

    // T-13: Beta needs no coin toss, only the die that chose it
    @Test
    void betaDoesNotTossTheCoin() {
        teleporterRolling(BETA_FACE, true).teleport(red1);
        verify(dice).roll();
        verify(coin, never()).tossHeads();
    }

    private Teleporter teleporterRolling(int dieFace, boolean heads) {
        when(dice.roll()).thenReturn(dieFace);
        when(coin.tossHeads()).thenReturn(heads);
        return new Teleporter(board, dice, coin, new TrackNavigator(), observer);
    }
}
