package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.port.GameEvents;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;

final class MoveBlockCommand extends MoveCommand {

    private final TrackNavigator navigator;

    MoveBlockCommand(MoveOption option, Board board, TrackNavigator navigator,
                     Teleporter teleporter, GameEvents.Moves listener) {
        super(option, board, teleporter, listener);
        this.navigator = navigator;
    }

    @Override
    public void execute() {
        Direction blockDirection = directionTravelled();
        for (Piece piece : option.movers()) {
            board.move(piece, option.destination());
            listener.onPieceMoved(piece, option.route(), blockDirection);
        }
        land();
    }

    private Direction directionTravelled() {
        int from = option.route().from().index();
        int clockwiseEnd = navigator.move(from, option.route().distance(), Direction.CLOCKWISE);
        return clockwiseEnd == option.destination().index() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
    }
}
