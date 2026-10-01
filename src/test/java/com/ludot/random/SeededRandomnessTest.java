package com.ludot.random;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeededRandomnessTest {

    // Design: dice stay within 1 to 6 and the same seed repeats the same rolls, so games can be replayed
    @Test
    void seededDiceAreValidAndRepeatable() {
        SeededRandomness first = new SeededRandomness(42);
        SeededRandomness second = new SeededRandomness(42);
        for (int roll = 0; roll < 100; roll++) {
            int value = first.roll();
            assertTrue(value >= 1 && value <= 6);
            assertEquals(value, second.roll());
        }
    }

    // T-10: the cell picker only ever picks one of the free cells it is given
    @Test
    void pickerChoosesOneOfTheCandidates() {
        SeededRandomness random = new SeededRandomness(7);
        List<Integer> candidates = List.of(3, 8, 15);
        for (int pick = 0; pick < 20; pick++) {
            assertTrue(candidates.contains(random.pick(candidates)));
        }
    }
}
