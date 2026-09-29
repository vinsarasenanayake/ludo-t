package com.ludot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MysteryCellTest {

    @Test
    @DisplayName("Null Object: before spawning, no cell is the mystery cell")
    void noMysteryCellIsNeverAtAnyCell() {
        assertFalse(NoMysteryCell.INSTANCE.isAt(0));
        assertFalse(NoMysteryCell.INSTANCE.isActive());
    }

    @Test
    void activeMysteryCellIsAtItsLocation() {
        MysteryCell mystery = new ActiveMysteryCell(17, 4);
        assertTrue(mystery.isAt(17));
    }

    @Test
    void activeMysteryCellIsNotAtOtherCells() {
        MysteryCell mystery = new ActiveMysteryCell(17, 4);
        assertFalse(mystery.isAt(18));
    }

    @Test
    void afterOneRoundItHasOneRoundFewerLeft() {
        ActiveMysteryCell mystery = new ActiveMysteryCell(17, 4);
        assertEquals(3, mystery.afterOneRound().roundsRemaining());
    }

    @Test
    void mysteryCellMustBeOnTheTrack() {
        assertThrows(IllegalArgumentException.class, () -> new ActiveMysteryCell(52, 4));
    }
}
