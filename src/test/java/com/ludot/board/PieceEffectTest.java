package com.ludot.board;

import org.junit.jupiter.api.Test;

import static com.ludot.board.BoardConstants.EFFECT_DURATION_ROUNDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceEffectTest {

    @Test
    void noEffectLeavesTheRollUnchanged() {
        PieceEffect none = PieceEffect.None.INSTANCE;
        none.endRound();
        assertEquals(4, none.adjustRoll(4));
        assertTrue(none.canMove());
        assertFalse(none.isExpired());
    }

    @Test
    void energisedDoublesTheRoll() {
        assertEquals(10, new PieceEffect.Energised().adjustRoll(5));
    }

    @Test
    void sickHalvesTheRollRoundedDown() {
        assertEquals(2, new PieceEffect.Sick().adjustRoll(5));
    }

    @Test
    void briefedPieceCannotMove() {
        PieceEffect briefing = new PieceEffect.Briefing();
        assertFalse(briefing.canMove());
        assertEquals(0, briefing.adjustRoll(6));
    }

    @Test
    void effectLastsExactlyFourMoreRounds() {
        PieceEffect sick = new PieceEffect.Sick();
        for (int round = 0; round < EFFECT_DURATION_ROUNDS; round++) {
            sick.endRound();
        }
        assertFalse(sick.isExpired());
        sick.endRound();
        assertTrue(sick.isExpired());
    }

    @Test
    void threesMustBeConsecutive() {
        PieceEffect briefing = new PieceEffect.Briefing();
        briefing.observeRoll(3);
        briefing.observeRoll(4);
        briefing.observeRoll(3);
        assertFalse(briefing.requiresReturnToBase());
    }
}