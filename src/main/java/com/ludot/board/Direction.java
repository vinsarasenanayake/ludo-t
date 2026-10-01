package com.ludot.board;

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

    public Direction opposite() {
        return this == CLOCKWISE ? COUNTER_CLOCKWISE : CLOCKWISE;
    }
}
