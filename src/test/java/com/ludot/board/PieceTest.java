package com.ludot.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceTest {

    private Piece red1;

    @BeforeEach
    void setUp() {
        red1 = new Piece(Colour.RED, 1);
    }

    @Test
    @DisplayName("Design: a custom exception stops a piece entering the board twice")
    void enteringWhenAlreadyOnTheBoardIsRejected() {
        red1.enterBoard(Direction.CLOCKWISE);
        assertThrows(Piece.IllegalMoveException.class, () -> red1.enterBoard(Direction.CLOCKWISE));
    }

    @Test
    @DisplayName("T-9: a captured piece loses all its information")
    void returnToBaseResetsEverything() {
        red1.enterBoard(Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        red1.recordApproachPass();
        red1.applyEffect(new PieceEffect.Energised());
        red1.returnToBase();
        assertTrue(red1.isInBase());
        assertFalse(red1.hasCaptured());
        assertEquals(0, red1.approachPasses());
        assertEquals(Direction.CLOCKWISE, red1.direction());
        assertEquals(5, red1.adjustRoll(5));
    }

    @Test
    @DisplayName("T-12 + A1: energised moves double; sick moves half, rounded down, at least one cell")
    void effectsAdjustTheRoll() {
        red1.applyEffect(new PieceEffect.Energised());
        assertEquals(10, red1.adjustRoll(5));
        PieceEffect sick = new PieceEffect.Sick();
        assertEquals(3, sick.adjustRoll(6));
        assertEquals(2, sick.adjustRoll(5));
        assertEquals(1, sick.adjustRoll(1));
    }

    @Test
    @DisplayName("T-12: an effect wears off after four rounds (Null Object takes over)")
    void expiredEffectIsReplacedByNoEffect() {
        red1.applyEffect(new PieceEffect.Energised());
        for (int round = 0; round < 4; round++) {
            red1.endRound();
        }
        assertEquals(5, red1.adjustRoll(5));
    }

    @Test
    @DisplayName("T-13 + A2: two threes in a row end the briefing and send the piece to base")
    void twoConsecutiveThreesEndTheBriefing() {
        PieceEffect briefing = new PieceEffect.Briefing();
        briefing.observeRoll(3);
        briefing.observeRoll(3);
        assertTrue(briefing.requiresReturnToBase());
    }
}
