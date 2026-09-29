package com.ludot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueTypesTest {

    @Test
    @DisplayName("Numbering starts at Yellow's starting square")
    void yellowStartsAtCellZero() {
        assertEquals(0, Colour.YELLOW.startCell());
    }

    @Test
    @DisplayName("Approach cell is two cells before X and wraps around the board")
    void yellowApproachWrapsToCellFifty() {
        assertEquals(50, Colour.YELLOW.approachCell());
    }

    @Test
    void redApproachIsCellTwentyFour() {
        assertEquals(24, Colour.RED.approachCell());
    }

    @Test
    @DisplayName("Dice passes clockwise: Red, Green, Yellow, Blue, then back to Red")
    void clockwiseOrderFollowsTheBrief() {
        assertEquals(Colour.GREEN, Colour.RED.nextClockwise());
        assertEquals(Colour.YELLOW, Colour.GREEN.nextClockwise());
        assertEquals(Colour.BLUE, Colour.YELLOW.nextClockwise());
        assertEquals(Colour.RED, Colour.BLUE.nextClockwise());
    }

    @Test
    void colourNamesForMessages() {
        assertEquals('R', Colour.RED.initial());
        assertEquals("Red", Colour.RED.title());
    }

    @Test
    void clockwiseStepsForward() {
        assertEquals(1, Direction.CLOCKWISE.stepSign());
    }

    @Test
    void counterClockwiseStepsBackward() {
        assertEquals(-1, Direction.COUNTER_CLOCKWISE.stepSign());
    }

    @Test
    void oppositeOfClockwiseIsCounterClockwise() {
        assertEquals(Direction.COUNTER_CLOCKWISE, Direction.CLOCKWISE.opposite());
    }

    @Test
    void baseIsDescribedAsBase() {
        assertEquals("Base", Position.base().describe(Colour.RED));
    }

    @Test
    void homeIsDescribedAsHome() {
        assertEquals("Home", Position.home().describe(Colour.RED));
    }

    @Test
    @DisplayName("Track cells are described by their square ID")
    void trackCellIsDescribedByItsNumber() {
        assertEquals("12", Position.onTrack(12).describe(Colour.RED));
    }

    @Test
    @DisplayName("Home straight cells use the [colour]homepath[n] format from the brief")
    void homeStraightUsesColourHomepathFormat() {
        assertEquals("redhomepath2", Position.inHomeStraight(2).describe(Colour.RED));
    }

    @Test
    void positionsWithSameZoneAndIndexAreEqual() {
        assertEquals(Position.onTrack(7), Position.onTrack(7));
    }

    @Test
    void basePositionIsInBase() {
        assertTrue(Position.base().isInBase());
    }

    @Test
    void trackCellOutsideTheBoardIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.onTrack(52));
    }

    @Test
    void homeStraightStepOutsideRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.inHomeStraight(5));
    }

    @Test
    void completedRouteIsNotCutShort() {
        assertFalse(Route.completed(Position.onTrack(1), Position.onTrack(4), 3, 0).isCutShortByBlock());
    }

    @Test
    void routeStoppedBeforeABlockIsCutShort() {
        Route.Blockage blockage = new Route.Blockage(Position.onTrack(6), new Piece(Colour.GREEN, 1));
        Route route = Route.completed(Position.onTrack(1), Position.onTrack(3), 2, 0).stoppedBy(blockage);
        assertTrue(route.isCutShortByBlock());
    }

    @Test
    @DisplayName("Null Object: before spawning, no cell is the mystery cell")
    void noMysteryCellIsNeverAtAnyCell() {
        assertFalse(MysteryCell.None.INSTANCE.isAt(0));
        assertFalse(MysteryCell.None.INSTANCE.isActive());
    }

    @Test
    void activeMysteryCellIsAtItsLocation() {
        assertTrue(new MysteryCell.Active(17, 4).isAt(17));
    }

    @Test
    void activeMysteryCellIsNotAtOtherCells() {
        assertFalse(new MysteryCell.Active(17, 4).isAt(18));
    }

    @Test
    void afterOneRoundItHasOneRoundFewerLeft() {
        assertEquals(3, new MysteryCell.Active(17, 4).afterOneRound().roundsRemaining());
    }

    @Test
    void mysteryCellMustBeOnTheTrack() {
        assertThrows(IllegalArgumentException.class, () -> new MysteryCell.Active(52, 4));
    }

    @Test
    @DisplayName("Rule T-11: die face 1 is Alpha")
    void faceOneIsAlpha() {
        assertEquals(MysteryCell.Destination.ALPHA, MysteryCell.Destination.fromDieFace(1));
    }

    @Test
    @DisplayName("Rule T-11: die face 6 is the approach cell")
    void faceSixIsApproach() {
        assertEquals(MysteryCell.Destination.APPROACH, MysteryCell.Destination.fromDieFace(6));
    }

    @Test
    void faceOutsideOneToSixIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MysteryCell.Destination.fromDieFace(7));
    }

    @Test
    void startIsDisplayedAsX() {
        assertEquals("X", MysteryCell.Destination.START.displayName());
    }
}
