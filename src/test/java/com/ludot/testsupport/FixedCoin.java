package com.ludot.testsupport;

import com.ludot.port.Coin;

public class FixedCoin implements Coin {

    private final boolean heads;

    public FixedCoin(boolean heads) {
        this.heads = heads;
    }

    @Override
    public boolean tossHeads() {
        return heads;
    }
}