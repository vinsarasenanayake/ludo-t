package com.ludot.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    @Test
    void clockwiseStepsForward() {
        assertEquals(1, Direction.CLOCKWISE.stepSign());
    }

    @Test
    void counterClockwiseStepsBackward() {
        assertEquals(-1, Direction.COUNTER_CLOCKWISE.stepSign());
    }

    @Test
    void oppositeOfClockwiseIsCounterClockwise() {
        assertEquals(Direction.COUNTER_CLOCKWISE, Direction.CLOCKWISE.opposite());
    }
}
