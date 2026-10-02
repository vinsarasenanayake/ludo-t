package com.ludot.board;

// T-1: the coin toss sets the direction
public enum Direction {
    CLOCKWISE(1, "clockwise"),
    COUNTER_CLOCKWISE(-1, "counterclockwise");

    private final int stepSign;
    private final String displayName;

    Direction(int stepSign, String displayName) {
        this.stepSign = stepSign;
        this.displayName = displayName;
    }

    public int stepSign() {
        return stepSign;
    }

    public String displayName() {
        return displayName;
    }

    // Used by Gamma (T-14)
    public Direction opposite() {
        return this == CLOCKWISE ? COUNTER_CLOCKWISE : CLOCKWISE;
    }
}