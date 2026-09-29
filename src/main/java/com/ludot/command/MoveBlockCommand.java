package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.port.MoveListener;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

public class MoveBlockCommand implements GameCommand {

    private final MoveOption option;
    private final Board board;
    private final TrackNavigator navigator;
    private final LandingHandler landingHandler;
    private final MoveListener listener;

    public MoveBlockCommand(MoveOption option, Board board, TrackNavigator navigator,
                            LandingHandler landingHandler, MoveListener listener) {
        this.option = option;
        this.board = board;
        this.navigator = navigator;
        this.landingHandler = landingHandler;
        this.listener = listener;
    }

    @Override
    public TurnOutcome execute() {
        Direction blockDirection = directionTravelled();
        for (Piece piece : option.movers()) {
            board.move(piece, option.destination());
            listener.onPieceMoved(piece, option.route(), blockDirection);
        }
        landingHandler.resolve(option);
        return new TurnOutcome(option.capturesAny(), option.capturesAny());
    }

    // Rule T-4: the block moves together, so the message shows the block's direction, not each piece's own.
    private Direction directionTravelled() {
        Position from = option.route().from();
        int clockwiseEnd = navigator.move(from.index(), option.route().distance(), Direction.CLOCKWISE);
        return clockwiseEnd == option.destination().index() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
    }
}
