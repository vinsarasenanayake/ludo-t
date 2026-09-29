package com.ludot.domain.effect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BriefingEffectTest {

    private BriefingEffect effect;

    @BeforeEach
    void setUp() {
        effect = new BriefingEffect();
    }

    @Test
    @DisplayName("Rule T-13: a piece at a briefing cannot move")
    void pieceCannotMove() {
        assertFalse(effect.canMove());
    }

    @Test
    void oneThreeDoesNotSendPieceToBase() {
        effect.observeRoll(3);
        assertFalse(effect.requiresReturnToBase());
    }

    @Test
    @DisplayName("Rule T-13 (assumption A11): two threes in a row send the piece to base")
    void twoConsecutiveThreesSendPieceToBase() {
        effect.observeRoll(3);
        effect.observeRoll(3);
        assertTrue(effect.requiresReturnToBase());
    }

    @Test
    void anotherRollBetweenThreesResetsTheCount() {
        effect.observeRoll(3);
        effect.observeRoll(5);
        effect.observeRoll(3);
        assertFalse(effect.requiresReturnToBase());
    }

    @Test
    void briefingEndsAfterFourRounds() {
        for (int round = 0; round < 4; round++) {
            effect.endRound();
        }
        assertTrue(effect.isExpired());
    }
}
