package com.ludot.movement;

final class NullMoveCommand implements GameCommand {

    @Override
    public void execute() {
    }

    @Override
    public boolean grantsBonusRoll() {
        return false;
    }

    @Override
    public boolean showsPlayerStatus() {
        return false;
    }

    @Override
    public boolean endsTurn() {
        return true;
    }
}