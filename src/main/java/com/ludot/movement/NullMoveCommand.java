package com.ludot.movement;

import com.ludot.board.Colour;

final class NullMoveCommand implements GameCommand {

    private final Colour colour;
    private final MoveEvents listener;

    NullMoveCommand(Colour colour, MoveEvents listener) {
        this.colour = colour;
        this.listener = listener;
    }

    @Override
    public void execute() {
        listener.onNoMovePossible(colour);
    }

    @Override
    public boolean grantsBonusRoll() {
        return false;
    }

    @Override
    public boolean showsPlayerStatus() {
        return false;
    }
}
