package com.ludot.domain.effect;

public interface PieceEffect {

    int adjustRoll(int roll);

    boolean canMove();

    void endRound();

    boolean isExpired();

    default void observeRoll(int roll) {
        // Most effects do not react to the player's rolls.
    }

    default boolean requiresReturnToBase() {
        return false;
    }
}
