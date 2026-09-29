package com.ludot.infrastructure;

import com.ludot.port.Coin;

import java.util.Random;

public class FairCoin implements Coin {

    private final Random random;

    public FairCoin(Random random) {
        this.random = random;
    }

    @Override
    public boolean tossHeads() {
        return random.nextBoolean();
    }
}
