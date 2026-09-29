package com.ludot.domain;

import static com.ludot.domain.BoardConstants.EFFECT_DURATION_ROUNDS;

public interface PieceEffect {

    int adjustRoll(int roll);

    boolean canMove();

    void endRound();

    boolean isExpired();

    default void observeRoll(int roll) {
    }

    default boolean requiresReturnToBase() {
        return false;
    }

    enum None implements PieceEffect {
        INSTANCE;

        @Override
        public int adjustRoll(int roll) {
            return roll;
        }

        @Override
        public boolean canMove() {
            return true;
        }

        @Override
        public void endRound() {
        }

        @Override
        public boolean isExpired() {
            return false;
        }
    }

    abstract class Timed implements PieceEffect {

        private int roundsRemaining = EFFECT_DURATION_ROUNDS;

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

    final class Energised extends Timed {

        private static final int SPEED_MULTIPLIER = 2;

        @Override
        public int adjustRoll(int roll) {
            return roll * SPEED_MULTIPLIER;
        }
    }

    final class Sick extends Timed {

        private static final int SPEED_DIVISOR = 2;
        private static final int MINIMUM_MOVE = 1;

        @Override
        public int adjustRoll(int roll) {
            return Math.max(MINIMUM_MOVE, roll / SPEED_DIVISOR);
        }
    }

    final class Briefing extends Timed {

        private static final int RETURN_TRIGGER_ROLL = 3;
        private static final int CONSECUTIVE_TRIGGERS_TO_RETURN = 2;
        private static final int NO_MOVEMENT = 0;

        private int consecutiveTriggerRolls;

        @Override
        public int adjustRoll(int roll) {
            return NO_MOVEMENT;
        }

        @Override
        public boolean canMove() {
            return false;
        }

        @Override
        public void observeRoll(int roll) {
            consecutiveTriggerRolls = roll == RETURN_TRIGGER_ROLL ? consecutiveTriggerRolls + 1 : 0;
        }

        @Override
        public boolean requiresReturnToBase() {
            return consecutiveTriggerRolls >= CONSECUTIVE_TRIGGERS_TO_RETURN;
        }
    }
}
