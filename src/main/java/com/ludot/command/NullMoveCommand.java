package com.ludot.command;

import com.ludot.domain.Colour;
import com.ludot.port.MoveListener;

// Null Object: used when no piece can move, so the engine never has to check for "no command".
public class NullMoveCommand implements GameCommand {

    private final Colour colour;
    private final MoveListener listener;

    public NullMoveCommand(Colour colour, MoveListener listener) {
        this.colour = colour;
        this.listener = listener;
    }

    @Override
    public TurnOutcome execute() {
        listener.onNoMovePossible(colour);
        return TurnOutcome.NOTHING;
    }
}
