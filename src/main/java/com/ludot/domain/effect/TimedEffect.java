package com.ludot.domain.effect;

// Rule T-12 and T-13: Alpha and Beta effects last four rounds, then wear off.
public abstract class TimedEffect implements PieceEffect {

    public static final int DURATION_ROUNDS = 4;

    private int roundsRemaining = DURATION_ROUNDS;

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public void endRound() {
        if (roundsRemaining > 0) {
            roundsRemaining--;
        }
    }

    @Override
    public boolean isExpired() {
        return roundsRemaining == 0;
    }

    public int roundsRemaining() {
        return roundsRemaining;
    }
}
