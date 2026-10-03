package com.ludot.mystery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MysteryCellTest {

    @Test
    void noMysteryCellIsNowhere() {
        MysteryCell none = MysteryCell.None.INSTANCE;
        assertFalse(none.isActive());
        assertFalse(none.isAt(10));
        assertSame(none, none.afterOneRound());
    }

    @Test
    void activeCellCountsDownEachRound() {
        MysteryCell cell = new MysteryCell.Active(10, 4);
        assertTrue(cell.isAt(10));
        assertEquals(new MysteryCell.Active(10, 3), cell.afterOneRound());
    }

    @Test
    void activeCellOffTheTrackIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MysteryCell.Active(52, 4));
    }

    @Test
    void impossibleDieFaceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(7));
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(0));
    }
}