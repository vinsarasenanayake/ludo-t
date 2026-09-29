package com.ludot.command;

public interface GameCommand {

    void execute();

    boolean grantsBonusRoll();

    boolean showsPlayerStatus();
}
