package com.ludot.board;

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
    private Piece green1;

    @BeforeEach
    void setUp() {
        board = new Board();
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
        green1 = new Piece(Colour.GREEN, 1);
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
    void sendingToBaseEmptiesTheCellAndResetsThePiece() {
        board.enter(red1, Direction.CLOCKWISE);
        red1.recordCapture();
        board.sendToBase(red1);
        assertFalse(red1.hasCaptured());
        assertTrue(board.occupantsAt(26).isEmpty());
        assertTrue(red1.isInBase());
    }

    @Test
    void piecesOfDifferentColoursAreNotABlock() {
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(26));
        assertFalse(board.isBlockOwnedBy(26, Colour.RED));
        assertFalse(board.isOpponentBlockAt(26, Colour.YELLOW));
    }

    @Test
    void blockSurvivesAnOpponentPieceOnTheSameCell() {
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(26));
        assertTrue(board.isBlockOwnedBy(26, Colour.RED));
        assertTrue(board.isOpponentBlockAt(26, Colour.GREEN));
        assertFalse(board.isOpponentBlockAt(26, Colour.RED));
    }

    @Test
    void opponentsAtListsOnlyOtherColours() {
        board.enter(red1, Direction.CLOCKWISE);
        board.enter(green1, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(26));
        assertEquals(List.of(green1), board.opponentsAt(26, Colour.RED));
    }

    @Test
    void emptyCellsExcludeOccupiedOnes() {
        assertEquals(52, board.emptyCellIndexes().size());
        board.enter(red1, Direction.CLOCKWISE);
        assertEquals(51, board.emptyCellIndexes().size());
        assertFalse(board.emptyCellIndexes().contains(26));
    }
}