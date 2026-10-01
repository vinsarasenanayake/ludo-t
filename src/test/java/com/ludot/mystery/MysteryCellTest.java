package com.ludot.mystery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MysteryCellTest {

    // Null Object: before T-10 spawns a cell, there is no mystery cell anywhere
    @Test
    void noMysteryCellIsNowhere() {
        MysteryCell none = MysteryCell.None.INSTANCE;
        assertFalse(none.isActive());
        assertFalse(none.isAt(10));
        assertSame(none, none.afterOneRound());
    }

    // T-10: an active cell counts down one round at a time
    @Test
    void activeCellCountsDownEachRound() {
        MysteryCell cell = new MysteryCell.Active(10, 4);
        assertTrue(cell.isAt(10));
        assertEquals(new MysteryCell.Active(10, 3), cell.afterOneRound());
    }

    // T-10: the mystery cell must be on the 52-cell standard path
    @Test
    void activeCellOffTheTrackIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MysteryCell.Active(52, 4));
    }

    // T-11: die faces 1 to 6 map to the six destinations in the brief's order
    @Test
    void dieFacesMapToTheSixDestinations() {
        assertEquals(MysteryCell.Destination.ALPHA, MysteryCell.Destination.fromDieFace(1));
        assertEquals(MysteryCell.Destination.BETA, MysteryCell.Destination.fromDieFace(2));
        assertEquals(MysteryCell.Destination.GAMMA, MysteryCell.Destination.fromDieFace(3));
        assertEquals(MysteryCell.Destination.BASE, MysteryCell.Destination.fromDieFace(4));
        assertEquals(MysteryCell.Destination.START, MysteryCell.Destination.fromDieFace(5));
        assertEquals(MysteryCell.Destination.APPROACH, MysteryCell.Destination.fromDieFace(6));
    }

    // Design: a face outside 1 to 6 is rejected
    @Test
    void impossibleDieFaceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(7));
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(0));
    }
}
