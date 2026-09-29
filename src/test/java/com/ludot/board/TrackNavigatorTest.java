package com.ludot.board;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ludot.board.BoardConstants.ALPHA_OFFSET;
import static com.ludot.board.BoardConstants.BETA_OFFSET;
import static com.ludot.board.BoardConstants.GAMMA_OFFSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackNavigatorTest {

    private final TrackNavigator navigator = new TrackNavigator();

    @Test
    @DisplayName("The track wraps round: 51 -> 0 clockwise and 0 -> 51 counter-clockwise")
    void movementWrapsRoundTheBoard() {
        assertEquals(0, navigator.step(51, Direction.CLOCKWISE));
        assertEquals(51, navigator.step(0, Direction.COUNTER_CLOCKWISE));
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
    @DisplayName("Rule T-1: counter-clockwise needs a full extra lap before the home straight")
    void counterClockwisePieceIsFurtherFromHome() {
        Piece clockwise = new Piece(Colour.YELLOW, 1);
        Piece counterClockwise = new Piece(Colour.YELLOW, 2);
        clockwise.enterBoard(Direction.CLOCKWISE);
        counterClockwise.enterBoard(Direction.COUNTER_CLOCKWISE);
        assertEquals(56, navigator.stepsToHome(clockwise));
        assertEquals(60, navigator.stepsToHome(counterClockwise));
    }
}
