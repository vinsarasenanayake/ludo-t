package com.ludot.domain.effect;

public enum NoEffect implements PieceEffect {
    INSTANCE;

    @Override
    public int adjustRoll(int roll) {
        return roll;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public void endRound() {
        // Nothing to count down: this effect never expires.
    }

    @Override
    public boolean isExpired() {
        return false;
    }
}