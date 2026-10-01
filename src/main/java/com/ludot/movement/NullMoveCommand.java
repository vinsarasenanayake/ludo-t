package com.ludot.movement;

/** Null Object: used when no piece can move with the roll, so the throw is simply ignored. */
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
