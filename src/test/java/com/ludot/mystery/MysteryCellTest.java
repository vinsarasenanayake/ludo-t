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

    // Design: a face outside 1 to 6 is rejected
    @Test
    void impossibleDieFaceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(7));
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(0));
    }
}
