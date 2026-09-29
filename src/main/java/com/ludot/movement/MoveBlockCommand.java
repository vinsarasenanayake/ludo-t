package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.Teleporter;
import com.ludot.rules.MoveOption;

final class MoveBlockCommand extends MoveCommand {

    private final TrackNavigator navigator;

    MoveBlockCommand(MoveOption option, Board board, TrackNavigator navigator,
                     Teleporter teleporter, MoveEvents listener) {
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
