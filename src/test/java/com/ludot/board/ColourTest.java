package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColourTest {

    // R9: the approach cell is two cells before X, wrapping round for Yellow
    @Test
    void approachCellIsTwoCellsBeforeTheStart() {
        assertEquals(50, Colour.YELLOW.approachCell());
        assertEquals(11, Colour.BLUE.approachCell());
        assertEquals(24, Colour.RED.approachCell());
        assertEquals(37, Colour.GREEN.approachCell());
    }

    // Brief 1.1: play passes clockwise, so after Red comes Green
    @Test
    void nextColourFollowsTheClockwiseOrder() {
        assertEquals(Colour.GREEN, Colour.RED.nextClockwise());
        assertEquals(Colour.YELLOW, Colour.GREEN.nextClockwise());
        assertEquals(Colour.BLUE, Colour.YELLOW.nextClockwise());
        assertEquals(Colour.RED, Colour.BLUE.nextClockwise());
    }
}
