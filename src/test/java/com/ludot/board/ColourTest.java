package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColourTest {

    // Brief 3.1: start cells are numbered from Yellow's X, 13 cells apart, clockwise
    @Test
    void startCellsFollowTheNumberedTrack() {
        assertEquals(0, Colour.YELLOW.startCell());
        assertEquals(13, Colour.BLUE.startCell());
        assertEquals(26, Colour.RED.startCell());
        assertEquals(39, Colour.GREEN.startCell());
    }

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

    // Brief 3.1: one full round of clockwise steps brings every colour back to itself
    @Test
    void fourStepsReturnToTheSameColour() {
        for (Colour colour : Colour.values()) {
            Colour current = colour;
            for (int step = 0; step < Colour.values().length; step++) {
                current = current.nextClockwise();
            }
            assertEquals(colour, current);
        }
    }
}
