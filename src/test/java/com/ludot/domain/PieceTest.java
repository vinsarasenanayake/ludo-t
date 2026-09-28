package com.ludot.domain;

import com.ludot.domain.effect.BriefingEffect;
import com.ludot.domain.effect.EnergisedEffect;
import com.ludot.domain.effect.NoEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    @DisplayName("Pieces are named by colour initial and number, e.g. R1")
    void nameCombinesColourInitialAndNumber() {
        assertEquals("R1", red1.name());
    }

    @Test
    void newPieceStartsInBase() {
        assertTrue(red1.isInBase());
    }

    @Test
    void newPieceHasNoEffect() {
        assertSame(NoEffect.INSTANCE, red1.effect());
    }

    @Test
    @DisplayName("Entering the board places the piece on its colour's X")
    void enterBoardPlacesPieceOnItsStartCell() {
        red1.enterBoard(Direction.CLOCKWISE);
        assertEquals(Position.onTrack(26), red1.position());
    }

    @Test
    @DisplayName("Rule T-1: the coin toss decides the direction")
    void enterBoardUsesTheChosenDirection() {
        red1.enterBoard(Direction.COUNTER_CLOCKWISE);
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    @Test
    void enteringWhenAlreadyOnTheBoardIsRejected() {
        red1.enterBoard(Direction.CLOCKWISE);
        assertThrows(IllegalMoveException.class, () -> red1.enterBoard(Direction.CLOCKWISE));
    }

    @Test
    void pieceThatIsHomeCannotBeMoved() {
        red1.moveTo(Position.home());
        assertThrows(IllegalMoveException.class, () -> red1.moveTo(Position.onTrack(3)));
    }

    @Test
    void pieceThatIsHomeCannotMove() {
        red1.moveTo(Position.home());
        assertFalse(red1.canMove());
    }

    @Test
    void newPieceHasNotCaptured() {
        assertFalse(red1.hasCaptured());
    }

    @Test
    void recordCaptureCountsCaptures() {
        red1.recordCapture();
        red1.recordCapture();
        assertEquals(2, red1.captureCount());
    }

    @Test
    void reverseDirectionFlipsClockwiseToCounterClockwise() {
        red1.enterBoard(Direction.CLOCKWISE);
        red1.reverseDirection();
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    @Test
    @DisplayName("Rule T-12: an energised piece moves double")
    void energisedPieceDoublesItsRoll() {
        red1.applyEffect(new EnergisedEffect());
        assertEquals(10, red1.adjustRoll(5));
    }

    @Test
    @DisplayName("Rule T-13: a piece at a briefing cannot move")
    void briefedPieceCannotMove() {
        red1.applyEffect(new BriefingEffect());
        assertFalse(red1.canMove());
    }

    @Test
    void expiredEffectIsReplacedByNoEffect() {
        red1.applyEffect(new EnergisedEffect());
        for (int round = 0; round < 4; round++) {
            red1.endRound();
        }
        assertSame(NoEffect.INSTANCE, red1.effect());
    }

    @Test
    @DisplayName("Rule T-9: a captured piece loses all its information")
    void returnToBaseResetsEverything() {
        red1.enterBoard(Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        red1.recordApproachPass();
        red1.applyEffect(new EnergisedEffect());

        red1.returnToBase();

        assertTrue(red1.isInBase());
        assertEquals(0, red1.captureCount());
        assertEquals(0, red1.approachPasses());
        assertEquals(Direction.CLOCKWISE, red1.direction());
        assertSame(NoEffect.INSTANCE, red1.effect());
    }
}