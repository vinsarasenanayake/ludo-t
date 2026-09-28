package com.ludot.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColourTest {

    @Test
    @DisplayName("Numbering starts at Yellow's starting square")
    void yellowStartsAtCellZero() {
        Assertions.assertEquals(0, Colour.YELLOW.startCell());
    }

    @Test
    @DisplayName("Approach cell is two cells before X and wraps around the board")
    void yellowApproachWrapsToCellFifty() {
        assertEquals(50, Colour.YELLOW.approachCell());
    }

    @Test
    void redApproachIsCellTwentyFour() {
        assertEquals(24, Colour.RED.approachCell());
    }

    @Test
    @DisplayName("Dice passes clockwise: Red, Green, Yellow, Blue, then back to Red")
    void clockwiseOrderFollowsTheBrief() {
        assertEquals(Colour.GREEN, Colour.RED.nextClockwise());
        assertEquals(Colour.YELLOW, Colour.GREEN.nextClockwise());
        assertEquals(Colour.BLUE, Colour.YELLOW.nextClockwise());
        assertEquals(Colour.RED, Colour.BLUE.nextClockwise());
    }

    @Test
    void initialIsUsedForPieceNames() {
        assertEquals('R', Colour.RED.initial());
    }
}