package com.ludot.domain.effect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SickEffectTest {

    private SickEffect effect;

    @BeforeEach
    void setUp() {
        effect = new SickEffect();
    }

    @Test
    @DisplayName("Rule T-12: a sick piece moves half the roll")
    void halvesAnEvenRoll() {
        assertEquals(3, effect.adjustRoll(6));
    }

    @Test
    @DisplayName("Assumption A10: odd rolls are rounded down")
    void roundsDownAnOddRoll() {
        assertEquals(2, effect.adjustRoll(5));
    }

    @Test
    @DisplayName("Assumption A10: a sick piece always moves at least one cell")
    void neverMovesLessThanOne() {
        assertEquals(1, effect.adjustRoll(1));
    }

    @Test
    void expiresAfterFourRounds() {
        for (int round = 0; round < 4; round++) {
            effect.endRound();
        }
        assertTrue(effect.isExpired());
    }
}
