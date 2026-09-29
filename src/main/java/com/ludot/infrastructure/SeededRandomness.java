package com.ludot.infrastructure;

import com.ludot.port.CellPicker;
import com.ludot.port.Coin;
import com.ludot.port.Dice;

import java.util.List;
import java.util.Random;

public class SeededRandomness implements Dice, Coin, CellPicker {

    private static final int DICE_FACES = 6;

    private final Random random;

    public SeededRandomness(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public int roll() {
        return random.nextInt(DICE_FACES) + 1;
    }

    @Override
    public boolean tossHeads() {
        return random.nextBoolean();
    }

    @Override
    public int pick(List<Integer> candidateCells) {
        return candidateCells.get(random.nextInt(candidateCells.size()));
    }
}
