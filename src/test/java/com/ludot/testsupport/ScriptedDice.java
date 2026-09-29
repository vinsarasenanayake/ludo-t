package com.ludot.testsupport;

import com.ludot.port.Dice;

public class ScriptedDice implements Dice {

    private final int[] rolls;
    private int nextRoll;

    public ScriptedDice(int... rolls) {
        this.rolls = rolls;
    }

    @Override
    public int roll() {
        if (nextRoll >= rolls.length) {
            throw new IllegalStateException("ScriptedDice ran out of rolls after " + rolls.length);
        }
        return rolls[nextRoll++];
    }
}
