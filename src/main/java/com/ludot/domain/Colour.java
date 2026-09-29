package com.ludot.domain;

import static com.ludot.domain.BoardConstants.APPROACH_OFFSET_FROM_START;
import static com.ludot.domain.BoardConstants.TRACK_SIZE;

// Board numbering: yellow's X is cell 0 and cells are counted clockwise up to 51.
// Declared in clockwise play order, so nextClockwise() is simply the next constant.
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

    public int startCell() {
        return startCell;
    }

    // The approach cell sits two cells before the colour's X, just before its home straight.
    public int approachCell() {
        return Math.floorMod(startCell - APPROACH_OFFSET_FROM_START, TRACK_SIZE);
    }

    public char initial() {
        return Character.toUpperCase(displayName.charAt(0));
    }

    public Colour nextClockwise() {
        Colour[] colours = values();
        return colours[(ordinal() + 1) % colours.length];
    }
}
