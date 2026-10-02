package com.ludot.board;

import static com.ludot.board.BoardConstants.HOME_STRAIGHT_LENGTH;
import static com.ludot.board.BoardConstants.TRACK_SIZE;

// Track maths: wrapping, distances, and steps to home
public final class TrackNavigator {

    public static final int NOT_ON_BOARD = Integer.MAX_VALUE;

    private static final int STEPS_FROM_APPROACH_TO_HOME = HOME_STRAIGHT_LENGTH + 1;
    private static final int CLOCKWISE_PASSES_TO_ENTER_HOME = 1;
    private static final int COUNTER_CLOCKWISE_PASSES_TO_ENTER_HOME = 2;
    private static final int LAP_WITHOUT_CAPTURE = TRACK_SIZE;

    public int step(int cell, Direction direction) {
        return move(cell, 1, direction);
    }

    // R8: floorMod wraps the track both ways
    public int move(int cell, int steps, Direction direction) {
        return Math.floorMod(cell + steps * direction.stepSign(), TRACK_SIZE);
    }

    public int distance(int fromCell, int toCell, Direction direction) {
        return Math.floorMod((toCell - fromCell) * direction.stepSign(), TRACK_SIZE);
    }

    public int passesNeededToEnterHome(Direction direction) {
        return direction == Direction.CLOCKWISE
                ? CLOCKWISE_PASSES_TO_ENTER_HOME
                : COUNTER_CLOCKWISE_PASSES_TO_ENTER_HOME;
    }

    public int stepsToHome(Piece piece) {
        if (piece.isHome()) {
            return 0;
        }
        if (piece.isInBase()) {
            return NOT_ON_BOARD;
        }
        if (piece.isInHomeStraight()) {
            return HOME_STRAIGHT_LENGTH - piece.position().index();
        }
        return stepsToHomeFromTrack(piece);
    }

    // T-7: no capture yet adds a lap
    private int stepsToHomeFromTrack(Piece piece) {
        return geometricStepsToHome(piece) + (piece.hasCaptured() ? 0 : LAP_WITHOUT_CAPTURE);
    }

    // Steps along the track, counting the approach passes still needed
    private int geometricStepsToHome(Piece piece) {
        int cell = piece.position().index();
        int toApproach = distance(cell, piece.colour().approachCell(), piece.direction());
        int passesStillNeeded = Math.max(0, passesNeededToEnterHome(piece.direction()) - piece.approachPasses());
        if (passesStillNeeded == 0) {
            return toApproach + STEPS_FROM_APPROACH_TO_HOME;
        }
        // Already on the approach, so the next pass is a full lap away
        int toNextPass = toApproach == 0 ? TRACK_SIZE : toApproach;
        int extraLaps = passesStillNeeded - 1;
        return toNextPass + extraLaps * TRACK_SIZE + STEPS_FROM_APPROACH_TO_HOME;
    }
}