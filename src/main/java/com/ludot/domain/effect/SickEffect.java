package com.ludot.domain.effect;

public class SickEffect extends TimedEffect {

    private static final int SPEED_DIVISOR = 2;
    // Assumption: half of a roll of 1 still moves one cell, so a sick piece is never frozen.
    private static final int MINIMUM_MOVE = 1;

    @Override
    public int adjustRoll(int roll) {
        return Math.max(MINIMUM_MOVE, roll / SPEED_DIVISOR);
    }
}
