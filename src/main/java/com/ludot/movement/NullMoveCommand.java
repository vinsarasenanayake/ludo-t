package com.ludot.movement;

// Null Object: no legal move, so the throw is ignored
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
