package com.ludot.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellTest {

    private Cell cell;
    private Piece red1;
    private Piece red2;
    private Piece green1;

    @BeforeEach
    void setUp() {
        cell = new Cell(10);
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
        green1 = new Piece(Colour.GREEN, 1);
    }

    @Test
    void newCellIsEmpty() {
        assertTrue(cell.isEmpty());
    }

    @Test
    void addedPieceIsAnOccupant() {
        cell.add(red1);
        assertEquals(List.of(red1), cell.occupants());
    }

    @Test
    void removedPieceIsNoLongerAnOccupant() {
        cell.add(red1);
        cell.remove(red1);
        assertTrue(cell.isEmpty());
    }

    @Test
    void onePieceIsNotABlock() {
        cell.add(red1);
        assertFalse(cell.isBlock());
    }

    @Test
    @DisplayName("Rule T-3: two pieces of the same colour form a block")
    void twoPiecesOfOneColourFormABlock() {
        cell.add(red1);
        cell.add(red2);
        assertTrue(cell.isBlockOwnedBy(Colour.RED));
    }

    @Test
    void piecesOfDifferentColoursAreNotABlock() {
        cell.add(red1);
        cell.add(green1);
        assertFalse(cell.isBlock());
    }

    @Test
    void opponentsExcludeThePlayersOwnPieces() {
        cell.add(red1);
        cell.add(green1);
        assertEquals(List.of(green1), cell.opponentsOf(Colour.RED));
    }

    @Test
    @DisplayName("Occupants list is a copy, so callers cannot change the cell")
    void occupantsCannotBeModifiedFromOutside() {
        cell.add(red1);
        assertThrows(UnsupportedOperationException.class, () -> cell.occupants().add(green1));
    }
}
