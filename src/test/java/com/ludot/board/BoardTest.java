package com.ludot.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    private Board board;
    private Piece red1;
    private Piece red2;
    private Piece green1;

    @BeforeEach
    void setUp() {
        board = new Board();
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
        green1 = new Piece(Colour.GREEN, 1);
    }

    @Test
    @DisplayName("R2: entering puts the piece on its colour's start cell X")
    void enteringPutsPieceOnItsStartCell() {
        board.enter(red1, Direction.CLOCKWISE);
        assertEquals(List.of(red1), board.occupantsAt(26));
    }

    @Test
    @DisplayName("R1: moving updates both cells and the piece")
    void movingUpdatesBothCellsAndThePiece() {
        board.enter(red1, Direction.CLOCKWISE);
        board.move(red1, Position.onTrack(30));
        assertTrue(board.occupantsAt(26).isEmpty());
        assertEquals(List.of(red1), board.occupantsAt(30));
        assertEquals(Position.onTrack(30), red1.position());
    }

    @Test
    @DisplayName("R6: a piece sent to base leaves the board")
    void sendingToBaseEmptiesTheCellAndResetsThePiece() {
        board.enter(red1, Direction.CLOCKWISE);
        board.sendToBase(red1);
        assertTrue(board.occupantsAt(26).isEmpty());
        assertTrue(red1.isInBase());
    }

    @Test
    @DisplayName("T-3: two pieces of the same colour on one cell form a block")
    void twoOwnPiecesOnOneCellFormABlock() {
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        assertTrue(board.isBlockOwnedBy(26, Colour.RED));
    }

    @Test
    @DisplayName("T-3: pieces of different colours are not a block")
    void piecesOfDifferentColoursAreNotABlock() {
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(26));
        assertFalse(board.isBlockAt(26));
    }

    @Test
    @DisplayName("Design: positions outside the 52-cell track are rejected")
    void positionsOutsideTheBoardAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.onTrack(52));
    }
}
