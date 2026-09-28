package com.ludot.rules;

import com.ludot.domain.Direction;

import static com.ludot.domain.BoardConstants.TRACK_SIZE;

public class TrackNavigator {

    public int step(int cell, Direction direction) {
        return move(cell, 1, direction);
    }

    public int move(int cell, int steps, Direction direction) {
        return Math.floorMod(cell + steps * direction.stepSign(), TRACK_SIZE);
    }

    public int distance(int fromCell, int toCell, Direction direction) {
        return Math.floorMod((toCell - fromCell) * direction.stepSign(), TRACK_SIZE);
    }
}