package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {

    // Base and home are shown by name
    @Test
    void baseAndHomeAreDescribedByName() {
        assertEquals("Base", Position.base().describe(Colour.RED));
        assertEquals("Home", Position.home().describe(Colour.RED));
    }

    // Home straight cells use the colour name
    @Test
    void homeStraightCellUsesTheColourName() {
        assertEquals("redhomepath2", Position.inHomeStraight(2).describe(Colour.RED));
        assertEquals("bluehomepath0", Position.inHomeStraight(0).describe(Colour.BLUE));
    }

    // Home straight cells are 0 to 4 only
    @Test
    void homeStraightStepOutsideZeroToFourIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.inHomeStraight(5));
        assertThrows(IllegalArgumentException.class, () -> Position.inHomeStraight(-1));
    }

    // Track cells are 0 to 51 only
    @Test
    void trackCellOutsideTheBoardIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.onTrack(-1));
        assertThrows(IllegalArgumentException.class, () -> Position.onTrack(52));
    }
}