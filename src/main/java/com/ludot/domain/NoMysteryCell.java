package com.ludot.domain;

// Null Object for "no mystery cell yet". It holds no state, so one shared instance is enough (enum Singleton).
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

    @Override
    public MysteryCell afterOneRound() {
        return this;
    }
}
