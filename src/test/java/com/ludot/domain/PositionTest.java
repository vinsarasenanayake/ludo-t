package com.ludot.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionTest {

    @Test
    void baseIsDescribedAsBase() {
        Assertions.assertEquals("Base", Position.base().describe(Colour.RED));
    }

    @Test
    void homeIsDescribedAsHome() {
        assertEquals("Home", Position.home().describe(Colour.RED));
    }

    @Test
    @DisplayName("Track cells are described by their square ID")
    void trackCellIsDescribedByItsNumber() {
        assertEquals("12", Position.onTrack(12).describe(Colour.RED));
    }

    @Test
    @DisplayName("Home straight cells use the [colour]homepath[n] format from the brief")
    void homeStraightUsesColourHomepathFormat() {
        assertEquals("redhomepath2", Position.inHomeStraight(2).describe(Colour.RED));
    }

    @Test
    void positionsWithSameZoneAndIndexAreEqual() {
        assertEquals(Position.onTrack(7), Position.onTrack(7));
    }

    @Test
    void newPositionIsInBaseWhenCreatedWithBase() {
        assertTrue(Position.base().isInBase());
    }

    @Test
    void trackCellOutsideTheBoardIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.onTrack(52));
    }

    @Test
    void homeStraightStepOutsideRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.inHomeStraight(5));
    }
}