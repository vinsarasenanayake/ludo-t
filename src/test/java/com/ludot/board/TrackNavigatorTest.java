package com.ludot.board;

import org.junit.jupiter.api.Test;

import static com.ludot.board.BoardConstants.ALPHA_OFFSET;
import static com.ludot.board.BoardConstants.BETA_OFFSET;
import static com.ludot.board.BoardConstants.GAMMA_OFFSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackNavigatorTest {

    private final TrackNavigator navigator = new TrackNavigator();

    // R8: the track wraps both ways
    @Test
    void movementWrapsRoundTheBoard() {
        assertEquals(0, navigator.step(51, Direction.CLOCKWISE));
        assertEquals(51, navigator.step(0, Direction.COUNTER_CLOCKWISE));
    }

    // T-11: Alpha, Beta, Gamma are 7, 25, 44
    @Test
    void teleportCellsAreCountedFromYellowApproach() {
        int yellowApproach = Colour.YELLOW.approachCell();
        assertEquals(7, navigator.move(yellowApproach, ALPHA_OFFSET, Direction.CLOCKWISE));
        assertEquals(25, navigator.move(yellowApproach, BETA_OFFSET, Direction.CLOCKWISE));
        assertEquals(44, navigator.move(yellowApproach, GAMMA_OFFSET, Direction.CLOCKWISE));
    }

    // T-1: counter-clockwise is further from home
    @Test
    void counterClockwisePieceIsFurtherFromHome() {
        Piece clockwise = new Piece(Colour.YELLOW, 1);
        Piece counterClockwise = new Piece(Colour.YELLOW, 2);
        clockwise.enterBoard(Direction.CLOCKWISE);
        counterClockwise.enterBoard(Direction.COUNTER_CLOCKWISE);
        clockwise.recordCapture();
        counterClockwise.recordCapture();
        assertEquals(56, navigator.stepsToHome(clockwise));
        assertEquals(60, navigator.stepsToHome(counterClockwise));
    }

    // T-7: no capture adds a lap
    @Test
    void pieceWithoutACaptureIsALapFurtherFromHome() {
        Piece nextToApproach = new Piece(Colour.YELLOW, 1);
        nextToApproach.enterBoard(Direction.CLOCKWISE);
        nextToApproach.moveTo(Position.onTrack(49));
        assertEquals(59, navigator.stepsToHome(nextToApproach));
        nextToApproach.recordCapture();
        assertEquals(7, navigator.stepsToHome(nextToApproach));
    }

    // R1 + T-1: distance depends on direction
    @Test
    void distanceDependsOnDirection() {
        assertEquals(4, navigator.distance(10, 14, Direction.CLOCKWISE));
        assertEquals(48, navigator.distance(10, 14, Direction.COUNTER_CLOCKWISE));
    }

    // R10: steps to home off the track
    @Test
    void stepsToHomeOffTheTrack() {
        Piece inBase = new Piece(Colour.RED, 1);
        Piece atHome = new Piece(Colour.RED, 2);
        Piece inStraight = new Piece(Colour.RED, 3);
        atHome.moveTo(Position.home());
        inStraight.moveTo(Position.inHomeStraight(2));
        assertEquals(TrackNavigator.NOT_ON_BOARD, navigator.stepsToHome(inBase));
        assertEquals(0, navigator.stepsToHome(atHome));
        assertEquals(3, navigator.stepsToHome(inStraight));
    }
}