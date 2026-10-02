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

    // T-11 + T-12: Alpha on heads energises
    @Test
    void alphaOnHeadsEnergisesThePiece() {
        teleporterRolling(ALPHA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(7), red1.position());
        assertEquals(10, red1.adjustRoll(5));
    }

    // T-12: Alpha on tails makes it sick
    @Test
    void alphaOnTailsMakesThePieceSick() {
        teleporterRolling(ALPHA_FACE, false).teleport(red1);
        assertEquals(2, red1.adjustRoll(5));
    }

    // T-13: Beta starts a briefing
    @Test
    void betaSendsToBriefing() {
        teleporterRolling(BETA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
        assertTrue(observer.hasEvent("teleport R1 BETA"));
    }

    // T-14: Gamma reverses a clockwise piece
    @Test
    void gammaReversesAClockwisePiece() {
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(44), red1.position());
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    // T-14: Gamma sends counter-clockwise to Beta
    @Test
    void gammaSendsACounterClockwisePieceToBeta() {
        red1.reverseDirection();
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
        assertTrue(observer.hasEvent("effect R1 SENT_TO_BETA"));
        assertFalse(observer.hasEvent("teleport R1 BETA"));
    }

    // Approach teleport counts as a pass
    @Test
    void approachSendsThePieceToItsApproachCell() {
        teleporterRolling(APPROACH_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(24), red1.position());
        assertEquals(1, red1.approachPasses());
    }

    // T-11 + T-9: Base resets the piece
    @Test
    void baseSendsThePieceToBase() {
        red1.recordCapture();
        teleporterRolling(BASE_FACE, true).teleport(red1);
        assertTrue(red1.isInBase());
        assertFalse(red1.hasCaptured());
        assertTrue(board.occupantsAt(30).isEmpty());
    }

    // T-11: X sends it to its start cell
    @Test
    void startSendsThePieceToItsX() {
        teleporterRolling(START_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(26), red1.position());
    }

    // T-11: teleport arrival does not capture
    @Test
    void teleportArrivalDoesNotCapture() {
        Piece green1 = new Piece(Colour.GREEN, 1);
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(26));
        teleporterRolling(START_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(26), red1.position());
        assertEquals(Position.onTrack(26), green1.position());
    }

    private Teleporter teleporterRolling(int dieFace, boolean heads) {
        when(dice.roll()).thenReturn(dieFace);
        when(coin.tossHeads()).thenReturn(heads);
        return new Teleporter(board, dice, coin, new TrackNavigator(), observer);
    }
}