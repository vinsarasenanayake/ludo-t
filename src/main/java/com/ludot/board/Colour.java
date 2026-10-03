package com.ludot.board;

import static com.ludot.board.BoardConstants.APPROACH_OFFSET_FROM_START;
import static com.ludot.board.BoardConstants.TRACK_SIZE;

public enum Colour {
    RED("red", 26),
    GREEN("green", 39),
    YELLOW("yellow", 0),
    BLUE("blue", 13);

    private final String displayName;
    private final int startCell;

    Colour(String displayName, int startCell) {
        this.displayName = displayName;
        this.startCell = startCell;
    }

    public String displayName() {
        return displayName;
    }

    public String title() {
        return initial() + displayName.substring(1);
    }

    public char initial() {
        return Character.toUpperCase(displayName.charAt(0));
    }

    public int startCell() {
        return startCell;
    }

    // Two cells before X; wraps for Yellow (0 -> 50)
    public int approachCell() {
        return Math.floorMod(startCell - APPROACH_OFFSET_FROM_START, TRACK_SIZE);
    }

    // The dice passes clockwise
    public Colour nextClockwise() {
        return switch (this) {
            case YELLOW -> BLUE;
            case BLUE -> RED;
            case RED -> GREEN;
            case GREEN -> YELLOW;
        };
    }
}