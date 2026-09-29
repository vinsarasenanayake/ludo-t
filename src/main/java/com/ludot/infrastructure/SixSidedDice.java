package com.ludot.infrastructure;

import com.ludot.port.Dice;

import java.util.Random;

public class SixSidedDice implements Dice {

    private static final int FACES = 6;

    private final Random random;

    public SixSidedDice(Random random) {
        this.random = random;
    }

    @Override
    public int roll() {
        return random.nextInt(FACES) + 1;
    }
}
