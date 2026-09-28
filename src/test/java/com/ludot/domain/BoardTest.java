package com.ludot.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    private Board board;
    private Piece red1;
    private Piece red2;

    @BeforeEach
    void setUp() {
        board = new Board();
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
    }

    @Test
    void boardHasFiftyTwoEmptyCells() {
        assertEquals(52, board.emptyCellIndexes().size());
    }

    @Test
    void enteringPutsPieceOnItsStartCell() {
        board.enter(red1, Direction.CLOCKWISE);
        assertEquals(List.of(red1), board.occupantsAt(26));
    }

    @Test
    void movingUpdatesBothCellsAndThePiece() {
        board.enter(red1, Direction.CLOCKWISE);
        board.move(red1, Position.onTrack(30));
        assertTrue(board.occupantsAt(26).isEmpty());
        assertEquals(List.of(red1), board.occupantsAt(30));
        assertEquals(Position.onTrack(30), red1.position());
    }

    @Test
    void movingIntoHomeStraightLeavesTheTrack() {
        board.enter(red1, Direction.CLOCKWISE);
        board.move(red1, Position.inHomeStraight(0));
        assertTrue(board.occupantsAt(26).isEmpty());
    }

    @Test
    void sendingToBaseEmptiesTheCellAndResetsThePiece() {
        board.enter(red1, Direction.CLOCKWISE);
        board.sendToBase(red1);
        assertTrue(board.occupantsAt(26).isEmpty());
        assertTrue(red1.isInBase());
    }

    @Test
    void twoOwnPiecesOnOneCellFormABlock() {
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        assertTrue(board.isBlockAt(26));
    }

    @Test
    void emptyCellsExcludeOccupiedCells() {
        board.enter(red1, Direction.CLOCKWISE);
        assertFalse(board.emptyCellIndexes().contains(26));
    }
}