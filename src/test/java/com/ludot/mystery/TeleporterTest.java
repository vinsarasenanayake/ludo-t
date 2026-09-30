package com.ludot.mystery;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.testsupport.TestDoubles.RecordingObserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ludot.testsupport.TestDoubles.fixedCoin;
import static com.ludot.testsupport.TestDoubles.scriptedDice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleporterTest {

    private static final int ALPHA_FACE = 1;
    private static final int BETA_FACE = 2;
    private static final int GAMMA_FACE = 3;
    private static final int APPROACH_FACE = 6;

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

    @Test
    @DisplayName("T-12: Alpha energises the piece on heads and makes it sick on tails")
    void alphaEffectDependsOnTheCoin() {
        Piece red2 = new Piece(Colour.RED, 2);
        board.enter(red2, Direction.CLOCKWISE);
        teleporterRolling(ALPHA_FACE, true).teleport(red1);
        teleporterRolling(ALPHA_FACE, false).teleport(red2);
        assertEquals(Position.onTrack(7), red1.position());
        assertEquals(10, red1.adjustRoll(5));
        assertEquals(2, red2.adjustRoll(5));
    }

    @Test
    @DisplayName("T-13: Beta sends the piece to a briefing, and the teleport is reported")
    void betaSendsToBriefing() {
        teleporterRolling(BETA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
        assertTrue(observer.hasEvent("teleport R1 BETA"));
    }

    @Test
    @DisplayName("T-14: Gamma turns a clockwise piece counter-clockwise")
    void gammaReversesAClockwisePiece() {
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(44), red1.position());
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    @Test
    @DisplayName("T-14: Gamma sends a counter-clockwise piece on to Beta")
    void gammaSendsACounterClockwisePieceToBeta() {
        red1.reverseDirection();
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
        assertTrue(observer.hasEvent("effect R1 SENT_TO_BETA"));
        assertFalse(observer.hasEvent("teleport R1 BETA"));
    }

    @Test
    @DisplayName("A6: teleporting to the approach counts as passing it")
    void approachSendsThePieceToItsApproachCell() {
        teleporterRolling(APPROACH_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(24), red1.position());
        assertEquals(1, red1.approachPasses());
    }

    private Teleporter teleporterRolling(int dieFace, boolean heads) {
        return new Teleporter(board, scriptedDice(dieFace), fixedCoin(heads), new TrackNavigator(), observer);
    }
}
