package com.ludot.rules;

import com.ludot.domain.Direction;
import com.ludot.domain.Piece;

import static com.ludot.domain.BoardConstants.HOME_STRAIGHT_LENGTH;
import static com.ludot.domain.BoardConstants.TRACK_SIZE;

public class TrackNavigator {

    public static final int NOT_ON_BOARD = Integer.MAX_VALUE;

    private static final int STEPS_FROM_APPROACH_TO_HOME = HOME_STRAIGHT_LENGTH + 1;
    private static final int CLOCKWISE_PASSES_TO_ENTER_HOME = 1;
    private static final int COUNTER_CLOCKWISE_PASSES_TO_ENTER_HOME = 2;

    public int step(int cell, Direction direction) {
        return move(cell, 1, direction);
    }

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

    private int stepsToHomeFromTrack(Piece piece) {
        int toApproach = distance(piece.position().index(), piece.colour().approachCell(), piece.direction());
        int passesStillNeeded = Math.max(0, passesNeededToEnterHome(piece.direction()) - piece.approachPasses());
        if (passesStillNeeded == 0) {
            return toApproach + STEPS_FROM_APPROACH_TO_HOME;
        }
        int firstArrival = toApproach == 0 ? TRACK_SIZE : toApproach;
        return firstArrival + (passesStillNeeded - 1) * TRACK_SIZE + STEPS_FROM_APPROACH_TO_HOME;
    }
}
