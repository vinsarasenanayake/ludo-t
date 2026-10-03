package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColourTest {

    @Test
    void approachCellIsTwoCellsBeforeTheStart() {
        assertEquals(50, Colour.YELLOW.approachCell());
        assertEquals(11, Colour.BLUE.approachCell());
        assertEquals(24, Colour.RED.approachCell());
        assertEquals(37, Colour.GREEN.approachCell());
    }

    @Test
    void nextColourFollowsTheClockwiseOrder() {
        assertEquals(Colour.GREEN, Colour.RED.nextClockwise());
        assertEquals(Colour.YELLOW, Colour.GREEN.nextClockwise());
        assertEquals(Colour.BLUE, Colour.YELLOW.nextClockwise());
        assertEquals(Colour.RED, Colour.BLUE.nextClockwise());
    }
}