package com.ludot.movement;

public interface GameCommand {

    void execute();

    boolean grantsBonusRoll();

    boolean showsPlayerStatus();

    // R7: a blocked or no-move throw ends the turn, even on a six
    default boolean endsTurn() {
        return false;
    }
}