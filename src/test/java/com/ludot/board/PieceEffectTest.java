package com.ludot.board;

import org.junit.jupiter.api.Test;

import static com.ludot.board.BoardConstants.EFFECT_DURATION_ROUNDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceEffectTest {

    // No effect keeps the roll and never expires
    @Test
    void noEffectLeavesTheRollUnchanged() {
        PieceEffect none = PieceEffect.None.INSTANCE;
        none.endRound();
        assertEquals(4, none.adjustRoll(4));
        assertTrue(none.canMove());
        assertFalse(none.isExpired());
    }

    // T-12: energised doubles the roll
    @Test
    void energisedDoublesTheRoll() {
        assertEquals(10, new PieceEffect.Energised().adjustRoll(5));
    }

    // T-12: sick halves it, rounded down
    @Test
    void sickHalvesTheRollRoundedDown() {
        assertEquals(2, new PieceEffect.Sick().adjustRoll(5));
    }

    // T-13: a briefed piece cannot move
    @Test
    void briefedPieceCannotMove() {
        PieceEffect briefing = new PieceEffect.Briefing();
        assertFalse(briefing.canMove());
        assertEquals(0, briefing.adjustRoll(6));
    }

    // T-12: lasts four rounds after teleport
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

    // T-13: the threes must be in a row
    @Test
    void threesMustBeConsecutive() {
        PieceEffect briefing = new PieceEffect.Briefing();
        briefing.observeRoll(3);
        briefing.observeRoll(4);
        briefing.observeRoll(3);
        assertFalse(briefing.requiresReturnToBase());
    }
}