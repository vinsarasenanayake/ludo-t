package com.ludot.rules;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackNavigatorStepsToHomeTest {

    private TrackNavigator navigator;
    private Piece yellow1;

    @BeforeEach
    void setUp() {
        navigator = new TrackNavigator();
        yellow1 = new Piece(Colour.YELLOW, 1);
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