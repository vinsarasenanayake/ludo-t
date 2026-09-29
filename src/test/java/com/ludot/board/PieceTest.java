package com.ludot.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceTest {

    private Piece red1;

    @BeforeEach
    void setUp() {
        red1 = new Piece(Colour.RED, 1);
    }

    @Test
    @DisplayName("Custom exception: a piece cannot enter the board twice")
    void enteringWhenAlreadyOnTheBoardIsRejected() {
        red1.enterBoard(Direction.CLOCKWISE);
        assertThrows(Piece.IllegalMoveException.class, () -> red1.enterBoard(Direction.CLOCKWISE));
    }

    @Test
    @DisplayName("Rule T-9: a captured piece loses all its information")
    void returnToBaseResetsEverything() {
        red1.enterBoard(Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        red1.recordApproachPass();
        red1.applyEffect(new PieceEffect.Energised());
        red1.returnToBase();
        assertTrue(red1.isInBase());
        assertEquals(0, red1.captureCount());
        assertEquals(0, red1.approachPasses());
        assertEquals(Direction.CLOCKWISE, red1.direction());
        assertSame(PieceEffect.None.INSTANCE, red1.effect());
    }

    @Test
    @DisplayName("Rule T-12 + assumption A10: energised moves double; sick moves half, rounded down, at least one cell")
    void effectsAdjustTheRoll() {
        red1.applyEffect(new PieceEffect.Energised());
        assertEquals(10, red1.adjustRoll(5));
        PieceEffect sick = new PieceEffect.Sick();
        assertEquals(3, sick.adjustRoll(6));
        assertEquals(2, sick.adjustRoll(5));
        assertEquals(1, sick.adjustRoll(1));
    }

    @Test
    @DisplayName("Rule T-12: an effect wears off after four rounds (Null Object takes over)")
    void expiredEffectIsReplacedByNoEffect() {
        red1.applyEffect(new PieceEffect.Energised());
        for (int round = 0; round < 4; round++) {
            red1.endRound();
        }
        assertSame(PieceEffect.None.INSTANCE, red1.effect());
    }

    @Test
    @DisplayName("Rule T-13 + assumption A11: two threes in a row send a briefed piece to base")
    void twoConsecutiveThreesEndTheBriefing() {
        PieceEffect briefing = new PieceEffect.Briefing();
        briefing.observeRoll(3);
        briefing.observeRoll(3);
        assertTrue(briefing.requiresReturnToBase());
    }
}
