package com.ludot.board;

import org.junit.jupiter.api.Test;

import static com.ludot.board.BoardConstants.EFFECT_DURATION_ROUNDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceEffectTest {

    // Null Object: no effect leaves the roll alone and never expires
    @Test
    void noEffectLeavesTheRollUnchanged() {
        PieceEffect none = PieceEffect.None.INSTANCE;
        none.endRound();
        assertEquals(4, none.adjustRoll(4));
        assertTrue(none.canMove());
        assertFalse(none.isExpired());
    }

    // T-12: an energised piece moves double the roll
    @Test
    void energisedDoublesTheRoll() {
        assertEquals(10, new PieceEffect.Energised().adjustRoll(5));
    }

    // T-12 + A1: a sick piece moves half the roll, rounded down
    @Test
    void sickHalvesTheRollRoundedDown() {
        assertEquals(2, new PieceEffect.Sick().adjustRoll(5));
    }

    // A1: a sick piece that rolls a one still moves one cell
    @Test
    void sickPieceStillMovesAtLeastOneCell() {
        assertEquals(1, new PieceEffect.Sick().adjustRoll(1));
    }

    // T-12: in the fourth round after the teleport, an energised piece still moves double
    @Test
    void energisedStillWorksInTheFourthRoundAfterTheTeleport() {
        PieceEffect energised = new PieceEffect.Energised();
        for (int round = 0; round < EFFECT_DURATION_ROUNDS; round++) {
            energised.endRound();
        }
        assertFalse(energised.isExpired());
        assertEquals(12, energised.adjustRoll(6));
    }

    // T-13: a briefed piece cannot move at all
    @Test
    void briefedPieceCannotMove() {
        PieceEffect briefing = new PieceEffect.Briefing();
        assertFalse(briefing.canMove());
        assertEquals(0, briefing.adjustRoll(6));
    }

    // T-12: after the teleport round, an effect lasts exactly four more rounds
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

    // T-13 + A2: the threes must be in a row; another number in between resets the count
    @Test
    void threesMustBeConsecutive() {
        PieceEffect briefing = new PieceEffect.Briefing();
        briefing.observeRoll(3);
        briefing.observeRoll(4);
        briefing.observeRoll(3);
        assertFalse(briefing.requiresReturnToBase());
    }
}
