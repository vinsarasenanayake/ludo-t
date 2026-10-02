package com.ludot.mystery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MysteryCellTest {

    // No mystery cell before one spawns
    @Test
    void noMysteryCellIsNowhere() {
        MysteryCell none = MysteryCell.None.INSTANCE;
        assertFalse(none.isActive());
        assertFalse(none.isAt(10));
        assertSame(none, none.afterOneRound());
    }

    // T-10: counts down each round
    @Test
    void activeCellCountsDownEachRound() {
        MysteryCell cell = new MysteryCell.Active(10, 4);
        assertTrue(cell.isAt(10));
        assertEquals(new MysteryCell.Active(10, 3), cell.afterOneRound());
    }

    // T-10: must be on the track
    @Test
    void activeCellOffTheTrackIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MysteryCell.Active(52, 4));
    }

    // Die faces outside 1 to 6 are rejected
    @Test
    void impossibleDieFaceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(7));
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(0));
    }
}