package com.ludot.command;

public record TurnOutcome(boolean capturedOpponent, boolean boardCountsChanged) {

    public static final TurnOutcome NOTHING = new TurnOutcome(false, false);

    public boolean grantsBonusRoll() {
        return capturedOpponent;
    }
}