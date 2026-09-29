package com.ludot.domain.effect;

public class EnergisedEffect extends TimedEffect {

    private static final int SPEED_MULTIPLIER = 2;

    @Override
    public int adjustRoll(int roll) {
        return roll * SPEED_MULTIPLIER;
    }
}
