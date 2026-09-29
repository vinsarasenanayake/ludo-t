package com.ludot.domain.effect;

// Rule T-13: the piece cannot move for four rounds. Assumption: "rolls three consecutively"
// means two threes in a row, which sends the piece back to base.
public class BriefingEffect extends TimedEffect {

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
        if (roll == RETURN_TRIGGER_ROLL) {
            consecutiveTriggerRolls++;
        } else {
            consecutiveTriggerRolls = 0;
        }
    }

    @Override
    public boolean requiresReturnToBase() {
        return consecutiveTriggerRolls >= CONSECUTIVE_TRIGGERS_TO_RETURN;
    }
}
