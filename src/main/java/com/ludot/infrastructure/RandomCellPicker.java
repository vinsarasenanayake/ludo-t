package com.ludot.infrastructure;

import com.ludot.port.CellPicker;

import java.util.List;
import java.util.Random;

public class RandomCellPicker implements CellPicker {

    private final Random random;

    public RandomCellPicker(Random random) {
        this.random = random;
    }

    @Override
    public int pick(List<Integer> candidateCells) {
        return candidateCells.get(random.nextInt(candidateCells.size()));
    }
}
