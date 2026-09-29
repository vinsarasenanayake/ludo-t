package com.ludot.domain;

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
        assertSame(PieceEffect.None.INSTANCE, red1.effect());
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
        assertThrows(Piece.IllegalMoveException.class, () -> red1.enterBoard(Direction.CLOCKWISE));
    }

    @Test
    void pieceThatIsHomeCannotBeMoved() {
        red1.moveTo(Position.home());
        assertThrows(Piece.IllegalMoveException.class, () -> red1.moveTo(Position.onTrack(3)));
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
        red1.applyEffect(new PieceEffect.Energised());
        assertEquals(10, red1.adjustRoll(5));
    }

    @Test
    @DisplayName("Rule T-13: a piece at a briefing cannot move")
    void briefedPieceCannotMove() {
        red1.applyEffect(new PieceEffect.Briefing());
        assertFalse(red1.canMove());
    }

    @Test
    void expiredEffectIsReplacedByNoEffect() {
        red1.applyEffect(new PieceEffect.Energised());
        endRounds(red1, 4);
        assertSame(PieceEffect.None.INSTANCE, red1.effect());
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
    void noEffectLeavesTheRollUnchanged() {
        assertEquals(5, PieceEffect.None.INSTANCE.adjustRoll(5));
    }

    @Test
    void noEffectLetsThePieceMove() {
        assertTrue(PieceEffect.None.INSTANCE.canMove());
    }

    @Test
    void noEffectNeverExpires() {
        endRounds(PieceEffect.None.INSTANCE, 10);
        assertFalse(PieceEffect.None.INSTANCE.isExpired());
    }

    @Test
    void noEffectNeverSendsThePieceToBase() {
        PieceEffect.None.INSTANCE.observeRoll(3);
        PieceEffect.None.INSTANCE.observeRoll(3);
        assertFalse(PieceEffect.None.INSTANCE.requiresReturnToBase());
    }

    @Test
    @DisplayName("Singleton: there is exactly one 'no effect' instance")
    void everyNoEffectReferenceIsTheSameInstance() {
        assertSame(PieceEffect.None.INSTANCE, PieceEffect.None.valueOf("INSTANCE"));
    }

    @Test
    @DisplayName("Rule T-12: an energised piece moves double the roll")
    void energisedDoublesTheRoll() {
        assertEquals(8, new PieceEffect.Energised().adjustRoll(4));
    }

    @Test
    void energisedPieceCanStillMove() {
        assertTrue(new PieceEffect.Energised().canMove());
    }

    @Test
    void energisedIsStillActiveAfterThreeRounds() {
        PieceEffect effect = new PieceEffect.Energised();
        endRounds(effect, 3);
        assertFalse(effect.isExpired());
    }

    @Test
    @DisplayName("Rule T-12: the effect lasts four rounds")
    void energisedExpiresAfterFourRounds() {
        PieceEffect effect = new PieceEffect.Energised();
        endRounds(effect, 4);
        assertTrue(effect.isExpired());
    }

    @Test
    @DisplayName("Rule T-12: a sick piece moves half the roll")
    void sickHalvesAnEvenRoll() {
        assertEquals(3, new PieceEffect.Sick().adjustRoll(6));
    }

    @Test
    @DisplayName("Assumption A10: odd rolls are rounded down")
    void sickRoundsDownAnOddRoll() {
        assertEquals(2, new PieceEffect.Sick().adjustRoll(5));
    }

    @Test
    @DisplayName("Assumption A10: a sick piece always moves at least one cell")
    void sickNeverMovesLessThanOne() {
        assertEquals(1, new PieceEffect.Sick().adjustRoll(1));
    }

    @Test
    void sickExpiresAfterFourRounds() {
        PieceEffect effect = new PieceEffect.Sick();
        endRounds(effect, 4);
        assertTrue(effect.isExpired());
    }

    @Test
    void oneThreeDoesNotEndTheBriefing() {
        PieceEffect effect = new PieceEffect.Briefing();
        effect.observeRoll(3);
        assertFalse(effect.requiresReturnToBase());
    }

    @Test
    @DisplayName("Rule T-13 (assumption A11): two threes in a row send the piece to base")
    void twoConsecutiveThreesSendPieceToBase() {
        PieceEffect effect = new PieceEffect.Briefing();
        effect.observeRoll(3);
        effect.observeRoll(3);
        assertTrue(effect.requiresReturnToBase());
    }

    @Test
    void anotherRollBetweenThreesResetsTheCount() {
        PieceEffect effect = new PieceEffect.Briefing();
        effect.observeRoll(3);
        effect.observeRoll(5);
        effect.observeRoll(3);
        assertFalse(effect.requiresReturnToBase());
    }

    @Test
    void briefingEndsAfterFourRounds() {
        PieceEffect effect = new PieceEffect.Briefing();
        endRounds(effect, 4);
        assertTrue(effect.isExpired());
    }

    private void endRounds(Piece piece, int rounds) {
        for (int round = 0; round < rounds; round++) {
            piece.endRound();
        }
    }

    private void endRounds(PieceEffect effect, int rounds) {
        for (int round = 0; round < rounds; round++) {
            effect.endRound();
        }
    }
}
