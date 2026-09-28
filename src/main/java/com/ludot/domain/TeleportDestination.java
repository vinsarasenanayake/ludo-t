package com.ludot.domain;

public enum TeleportDestination {
    ALPHA("Alpha"),
    BETA("Beta"),
    GAMMA("Gamma"),
    BASE("Base"),
    START("X"),
    APPROACH("Approach");

    private final String displayName;

    TeleportDestination(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static TeleportDestination fromDieFace(int face) {
        TeleportDestination[] destinations = values();
        if (face < 1 || face > destinations.length) {
            throw new IllegalArgumentException("Die face must be 1-6 but was " + face);
        }
        return destinations[face - 1];
    }
}