package com.ludot.domain;

public enum NoMysteryCell implements MysteryCell {
    INSTANCE;

    private static final int NO_LOCATION = -1;

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isAt(int cell) {
        return false;
    }

    @Override
    public int location() {
        return NO_LOCATION;
    }

    @Override
    public int roundsRemaining() {
        return 0;
    }
}