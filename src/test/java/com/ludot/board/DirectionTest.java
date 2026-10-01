package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    // T-14: reversing flips the direction
    @Test
    void oppositeOfClockwiseIsCounterClockwise() {
        assertEquals(Direction.COUNTER_CLOCKWISE, Direction.CLOCKWISE.opposite());
    }

    // T-14: reversing twice restores the original direction
    @Test
    void oppositeOfOppositeIsTheOriginal() {
        assertEquals(Direction.CLOCKWISE, Direction.CLOCKWISE.opposite().opposite());
    }
}
