package com.ludot.domain;

import static com.ludot.domain.BoardConstants.APPROACH_OFFSET_FROM_START;
import static com.ludot.domain.BoardConstants.TRACK_SIZE;

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
        return Character.toUpperCase(displayName.charAt(0)) + displayName.substring(1);
    }

    public char initial() {
        return Character.toUpperCase(displayName.charAt(0));
    }

    public int startCell() {
        return startCell;
    }

    public int approachCell() {
        return Math.floorMod(startCell - APPROACH_OFFSET_FROM_START, TRACK_SIZE);
    }

    public Colour nextClockwise() {
        Colour[] colours = values();
        return colours[(ordinal() + 1) % colours.length];
    }
}
