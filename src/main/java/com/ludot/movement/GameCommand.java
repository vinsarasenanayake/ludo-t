package com.ludot.movement;

public interface GameCommand {

    void execute();

    boolean grantsBonusRoll();

    boolean showsPlayerStatus();
}
