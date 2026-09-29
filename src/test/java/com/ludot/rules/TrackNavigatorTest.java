package com.ludot.rules;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ludot.domain.BoardConstants.ALPHA_OFFSET;
import static com.ludot.domain.BoardConstants.BETA_OFFSET;
import static com.ludot.domain.BoardConstants.GAMMA_OFFSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackNavigatorTest {

    private final TrackNavigator navigator = new TrackNavigator();
    private final Piece yellow1 = new Piece(Colour.YELLOW, 1);

    @Test
    void clockwiseStepMovesToNextCell() {
        assertEquals(11, navigator.step(10, Direction.CLOCKWISE));
    }

    @Test
    @DisplayName("Clockwise from the last cell wraps to cell 0")
    void clockwiseStepWrapsFromFiftyOneToZero() {
        assertEquals(0, navigator.step(51, Direction.CLOCKWISE));
    }

    @Test
    @DisplayName("Counter-clockwise from cell 0 wraps to cell 51")
    void counterClockwiseStepWrapsFromZeroToFiftyOne() {
        assertEquals(51, navigator.step(0, Direction.COUNTER_CLOCKWISE));
    }

    @Test
    void moveSeveralStepsCounterClockwise() {
        assertEquals(49, navigator.move(1, 4, Direction.COUNTER_CLOCKWISE));
    }

    @Test
    void distanceClockwiseAcrossTheWrap() {
        assertEquals(3, navigator.distance(50, 1, Direction.CLOCKWISE));
    }

    @Test
    void distanceCounterClockwiseAcrossTheWrap() {
        assertEquals(3, navigator.distance(1, 50, Direction.COUNTER_CLOCKWISE));
    }

    @Test
    @DisplayName("Rule T-11: Alpha, Beta, Gamma are cells 7, 25, 44 counted from Yellow's approach")
    void teleportCellsAreCountedFromYellowApproach() {
        int yellowApproach = Colour.YELLOW.approachCell();
        assertEquals(7, navigator.move(yellowApproach, ALPHA_OFFSET, Direction.CLOCKWISE));
        assertEquals(25, navigator.move(yellowApproach, BETA_OFFSET, Direction.CLOCKWISE));
        assertEquals(44, navigator.move(yellowApproach, GAMMA_OFFSET, Direction.CLOCKWISE));
    }

    @Test
    void pieceInBaseIsNotOnTheBoard() {
        assertEquals(TrackNavigator.NOT_ON_BOARD, navigator.stepsToHome(yellow1));
    }

    @Test
    @DisplayName("Clockwise from X: 50 cells to the approach, then 6 more to Home")
    void clockwisePieceOnStartIsFiftySixFromHome() {
        yellow1.enterBoard(Direction.CLOCKWISE);
        assertEquals(56, navigator.stepsToHome(yellow1));
    }

    @Test
    @DisplayName("Rule T-1: counter-clockwise needs a full extra lap before the home straight")
    void counterClockwisePieceOnStartIsSixtyFromHome() {
        yellow1.enterBoard(Direction.COUNTER_CLOCKWISE);
        assertEquals(60, navigator.stepsToHome(yellow1));
    }

    @Test
    void pieceOnHomepathTwoIsThreeFromHome() {
        yellow1.moveTo(Position.inHomeStraight(2));
        assertEquals(3, navigator.stepsToHome(yellow1));
    }

    @Test
    void clockwiseNeedsOnePassAndCounterClockwiseNeedsTwo() {
        assertEquals(1, navigator.passesNeededToEnterHome(Direction.CLOCKWISE));
        assertEquals(2, navigator.passesNeededToEnterHome(Direction.COUNTER_CLOCKWISE));
    }
}
