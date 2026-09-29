package com.ludot.command;

import com.ludot.domain.Colour;
import com.ludot.port.GameObserver;

public class NullMoveCommand implements GameCommand {

    private final Colour colour;
    private final GameObserver observer;

    public NullMoveCommand(Colour colour, GameObserver observer) {
        this.colour = colour;
        this.observer = observer;
    }

    @Override
    public TurnOutcome execute() {
        observer.onNoMovePossible(colour);
        return TurnOutcome.NOTHING;
    }
}