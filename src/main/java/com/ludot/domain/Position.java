package com.ludot.domain;

import static com.ludot.domain.BoardConstants.HOME_STRAIGHT_LENGTH;
import static com.ludot.domain.BoardConstants.TRACK_SIZE;

public record Position(Zone zone, int index) {

    private static final int NO_INDEX = -1;

    public Position {
        if (zone == Zone.TRACK && isOutside(index, TRACK_SIZE)) {
            throw new IllegalArgumentException("Track cell must be 0-51 but was " + index);
        }
        if (zone == Zone.HOME_STRAIGHT && isOutside(index, HOME_STRAIGHT_LENGTH)) {
            throw new IllegalArgumentException("Home straight step must be 0-4 but was " + index);
        }
    }

    public static Position base() {
        return new Position(Zone.BASE, NO_INDEX);
    }

    public static Position home() {
        return new Position(Zone.HOME, NO_INDEX);
    }

    public static Position onTrack(int cell) {
        return new Position(Zone.TRACK, cell);
    }

    public static Position inHomeStraight(int step) {
        return new Position(Zone.HOME_STRAIGHT, step);
    }

    public boolean isInBase() {
        return zone == Zone.BASE;
    }

    public boolean isOnTrack() {
        return zone == Zone.TRACK;
    }

    public boolean isInHomeStraight() {
        return zone == Zone.HOME_STRAIGHT;
    }

    public boolean isHome() {
        return zone == Zone.HOME;
    }

    public String describe(Colour owner) {
        return switch (zone) {
            case BASE -> "Base";
            case HOME -> "Home";
            case TRACK -> String.valueOf(index);
            case HOME_STRAIGHT -> owner.displayName() + "homepath" + index;
        };
    }

    private static boolean isOutside(int value, int size) {
        return value < 0 || value >= size;
    }
}