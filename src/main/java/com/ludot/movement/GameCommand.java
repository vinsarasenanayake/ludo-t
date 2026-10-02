package com.ludot.movement;

// Command: a move the turn can run and then ask about
public interface GameCommand {

    void execute();

    boolean grantsBonusRoll();

    boolean showsPlayerStatus();

    // R7: a blocked or no-move throw ends the turn, even on a six
    default boolean endsTurn() {
        return false;
    }
}
