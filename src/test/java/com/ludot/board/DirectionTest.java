package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    @Test
    void oppositeOfClockwiseIsCounterClockwise() {
        assertEquals(Direction.COUNTER_CLOCKWISE, Direction.CLOCKWISE.opposite());
    }
}