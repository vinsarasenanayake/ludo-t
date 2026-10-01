package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    // T-1: clockwise steps forward, counter-clockwise steps back
    @Test
    void stepSignMatchesTheDirection() {
        assertEquals(1, Direction.CLOCKWISE.stepSign());
        assertEquals(-1, Direction.COUNTER_CLOCKWISE.stepSign());
    }

    // T-14: reversing flips the direction, and reversing twice restores it
    @Test
    void oppositeOfOppositeIsTheOriginal() {
        assertEquals(Direction.COUNTER_CLOCKWISE, Direction.CLOCKWISE.opposite());
        assertEquals(Direction.CLOCKWISE, Direction.CLOCKWISE.opposite().opposite());
    }

    // Brief 3.1: the direction names printed in move messages
    @Test
    void directionNamesUsedInMessages() {
        assertEquals("clockwise", Direction.CLOCKWISE.displayName());
        assertEquals("counter-clockwise", Direction.COUNTER_CLOCKWISE.displayName());
    }
}
