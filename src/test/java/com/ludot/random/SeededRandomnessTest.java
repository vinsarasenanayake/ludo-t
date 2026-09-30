package com.ludot.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeededRandomnessTest {

    @Test
    @DisplayName("Design: dice stay within 1 to 6 and the same seed repeats the same rolls, so games can be replayed")
    void seededDiceAreValidAndRepeatable() {
        SeededRandomness first = new SeededRandomness(42);
        SeededRandomness second = new SeededRandomness(42);
        for (int roll = 0; roll < 100; roll++) {
            int value = first.roll();
            assertTrue(value >= 1 && value <= 6);
            assertEquals(value, second.roll());
        }
    }
}
