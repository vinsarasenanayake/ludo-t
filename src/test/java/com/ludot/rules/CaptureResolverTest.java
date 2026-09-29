package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptureResolverTest {

    private Board board;
    private CaptureResolver captureResolver;
    private Piece red1;
    private Piece green1;
    private Piece green2;

    @BeforeEach
    void setUp() {
        board = new Board();
        captureResolver = new CaptureResolver(board);
        red1 = new Piece(Colour.RED, 1);
        green1 = new Piece(Colour.GREEN, 1);
        green2 = new Piece(Colour.GREEN, 2);
    }

    @Test
    @DisplayName("Rule 6: a single opponent piece on the landing cell is a victim")
    void singleOpponentIsAVictim() {
        board.enter(green1, Direction.CLOCKWISE);
        assertEquals(List.of(green1), captureResolver.findVictims(Colour.RED, 39, 1));
    }

    @Test
    void ownPiecesAreNeverVictims() {
        board.enter(red1, Direction.CLOCKWISE);
        assertTrue(captureResolver.findVictims(Colour.RED, 26, 1).isEmpty());
    }

    @Test
    @DisplayName("Rule T-3: one piece cannot capture a block")
    void singlePieceCannotCaptureABlock() {
        board.enter(green1, Direction.CLOCKWISE);
        board.enter(green2, Direction.CLOCKWISE);
        assertTrue(captureResolver.findVictims(Colour.RED, 39, 1).isEmpty());
    }

    @Test
    @DisplayName("Rule T-8: a block of the same size captures a block")
    void sameSizeBlockCapturesABlock() {
        board.enter(green1, Direction.CLOCKWISE);
        board.enter(green2, Direction.CLOCKWISE);
        assertEquals(2, captureResolver.findVictims(Colour.RED, 39, 2).size());
    }

    @Test
    @DisplayName("Rule 6 + T-9: victims go to base and are reset")
    void captureSendsVictimsToBase() {
        board.enter(green1, Direction.CLOCKWISE);
        captureResolver.capture(List.of(red1), List.of(green1));
        assertTrue(green1.isInBase());
        assertTrue(board.occupantsAt(39).isEmpty());
    }

    @Test
    @DisplayName("Rule T-8: every capturing piece gets one more capture")
    void captureIncrementsEachAttackersCount() {
        board.enter(green1, Direction.CLOCKWISE);
        red1.moveTo(Position.onTrack(39));
        captureResolver.capture(List.of(red1), List.of(green1));
        assertEquals(1, red1.captureCount());
    }
}
