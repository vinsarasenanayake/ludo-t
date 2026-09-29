package com.ludot.command;

public record TurnOutcome(boolean capturedOpponent, boolean showsPlayerStatus) {

    public static final TurnOutcome NOTHING = new TurnOutcome(false, false);

    // Rule T-2: capturing an opponent earns another roll.
    public boolean grantsBonusRoll() {
        return capturedOpponent;
    }
}
