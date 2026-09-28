package com.ludot.domain.effect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoEffectTest {

    @Test
    void rollIsUnchanged() {
        assertEquals(5, NoEffect.INSTANCE.adjustRoll(5));
    }

    @Test
    void pieceCanMove() {
        assertTrue(NoEffect.INSTANCE.canMove());
    }

    @Test
    void neverExpiresEvenAfterManyRounds() {
        for (int round = 0; round < 10; round++) {
            NoEffect.INSTANCE.endRound();
        }
        assertFalse(NoEffect.INSTANCE.isExpired());
    }

    @Test
    void neverSendsPieceToBase() {
        NoEffect.INSTANCE.observeRoll(3);
        NoEffect.INSTANCE.observeRoll(3);
        assertFalse(NoEffect.INSTANCE.requiresReturnToBase());
    }

    @Test
    @DisplayName("Singleton: there is exactly one NoEffect instance")
    void everyReferenceIsTheSameInstance() {
        PieceEffect first = NoEffect.INSTANCE;
        PieceEffect second = NoEffect.valueOf("INSTANCE");
        assertSame(first, second);
    }
}