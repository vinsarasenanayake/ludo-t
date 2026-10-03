package com.ludot.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ludot.board.BoardConstants.EFFECT_DURATION_ROUNDS;
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
    void enteringWhenAlreadyOnTheBoardIsRejected() {
        red1.enterBoard(Direction.CLOCKWISE);
        assertThrows(Piece.IllegalMoveException.class, () -> red1.enterBoard(Direction.CLOCKWISE));
    }

    @Test
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
        assertEquals(Direction.CLOCKWISE, red1.originalDirection());
        assertEquals(5, red1.adjustRoll(5));
    }

    @Test
    void reversingKeepsTheOriginalDirection() {
        red1.enterBoard(Direction.CLOCKWISE);
        red1.reverseDirection();
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
        assertEquals(Direction.CLOCKWISE, red1.originalDirection());
        red1.restoreOriginalDirection();
        assertEquals(Direction.CLOCKWISE, red1.direction());
    }

    @Test
    void expiredEffectIsReplacedByNoEffect() {
        red1.applyEffect(new PieceEffect.Energised());
        for (int round = 0; round <= EFFECT_DURATION_ROUNDS; round++) {
            red1.endRound();
        }
        assertEquals(5, red1.adjustRoll(5));
    }

    @Test
    void twoConsecutiveThreesEndTheBriefing() {
        red1.applyEffect(new PieceEffect.Briefing());
        red1.observeRoll(3);
        red1.observeRoll(3);
        assertTrue(red1.requiresReturnToBase());
    }

    @Test
    void pieceAtHomeCannotMoveAgain() {
        red1.moveTo(Position.home());
        assertFalse(red1.canMove());
        assertThrows(Piece.IllegalMoveException.class, () -> red1.moveTo(Position.onTrack(1)));
    }
}