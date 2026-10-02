package com.ludot.board;

import static com.ludot.board.BoardConstants.EFFECT_DURATION_ROUNDS;

// Each effect changes how a piece moves (T-12, T-13)
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

    // Null Object and Singleton: no effect
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

    // Lasts the rest of the teleport round, then four full rounds
    abstract class Timed implements PieceEffect {

        private int roundsRemaining = EFFECT_DURATION_ROUNDS;
        private boolean inTeleportRound = true;

        @Override
        public boolean canMove() {
            return true;
        }

        @Override
        public void endRound() {
            if (inTeleportRound) {
                inTeleportRound = false;
            } else if (roundsRemaining > 0) {
                roundsRemaining--;
            }
        }

        @Override
        public boolean isExpired() {
            return roundsRemaining == 0;
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

        @Override
        public int adjustRoll(int roll) {
            // Rounded down, so a 1 gives no move
            return roll / SPEED_DIVISOR;
        }
    }

    // T-13: the piece cannot move while in a briefing
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

        // Counts threes in a row; any other roll resets it
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
