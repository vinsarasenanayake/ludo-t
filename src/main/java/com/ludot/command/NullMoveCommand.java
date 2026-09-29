package com.ludot.command;

import com.ludot.domain.Colour;
import com.ludot.port.GameEvents;

final class NullMoveCommand implements GameCommand {

    private final Colour colour;
    private final GameEvents.Moves listener;

    NullMoveCommand(Colour colour, GameEvents.Moves listener) {
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
