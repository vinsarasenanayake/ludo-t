package com.ludot.domain.effect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergisedEffectTest {

    private EnergisedEffect effect;

    @BeforeEach
    void setUp() {
        effect = new EnergisedEffect();
    }

    @Test
    @DisplayName("Rule T-12: an energised piece moves double the roll")
    void doublesTheRoll() {
        assertEquals(8, effect.adjustRoll(4));
    }

    @Test
    void pieceCanStillMove() {
        assertTrue(effect.canMove());
    }

    @Test
    void stillActiveAfterThreeRounds() {
        endRounds(3);
        assertFalse(effect.isExpired());
    }

    @Test
    @DisplayName("Rule T-12: the effect lasts four rounds")
    void expiresAfterFourRounds() {
        endRounds(4);
        assertTrue(effect.isExpired());
    }

    private void endRounds(int rounds) {
        for (int round = 0; round < rounds; round++) {
            effect.endRound();
        }
    }
}