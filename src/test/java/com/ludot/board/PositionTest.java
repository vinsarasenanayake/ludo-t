package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {

    // Brief 3.1: base and home are shown by name
    @Test
    void baseAndHomeAreDescribedByName() {
        assertEquals("Base", Position.base().describe(Colour.RED));
        assertEquals("Home", Position.home().describe(Colour.RED));
    }

    // Brief 3.1: a track cell is shown as its number
    @Test
    void trackCellIsDescribedByItsNumber() {
        assertEquals("26", Position.onTrack(26).describe(Colour.RED));
    }

    // Brief 3.1: home straight cells are named [colour]homepath[cell number]
    @Test
    void homeStraightCellUsesTheColourName() {
        assertEquals("redhomepath2", Position.inHomeStraight(2).describe(Colour.RED));
        assertEquals("bluehomepath0", Position.inHomeStraight(0).describe(Colour.BLUE));
    }

    // Design: a home straight has only five cells, 0 to 4
    @Test
    void homeStraightStepOutsideZeroToFourIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.inHomeStraight(5));
        assertThrows(IllegalArgumentException.class, () -> Position.inHomeStraight(-1));
    }

    // Design: track cells outside 0 to 51 are rejected
    @Test
    void trackCellOutsideTheBoardIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.onTrack(-1));
        assertThrows(IllegalArgumentException.class, () -> Position.onTrack(52));
    }

    // Design: a record compares by value, so two equal positions are equal
    @Test
    void positionsWithTheSameValuesAreEqual() {
        assertEquals(Position.onTrack(7), Position.onTrack(7));
    }
}
