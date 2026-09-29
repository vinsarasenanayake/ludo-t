package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.port.GameObserver;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

public class MoveBlockCommand implements GameCommand {

    private final MoveOption option;
    private final Board board;
    private final TrackNavigator navigator;
    private final LandingHandler landingHandler;
    private final GameObserver observer;

    public MoveBlockCommand(MoveOption option, Board board, TrackNavigator navigator,
                            LandingHandler landingHandler, GameObserver observer) {
        this.option = option;
        this.board = board;
        this.navigator = navigator;
        this.landingHandler = landingHandler;
        this.observer = observer;
    }

    @Override
    public TurnOutcome execute() {
        Position from = option.route().from();
        Direction blockDirection = directionTravelled(from);
        for (Piece piece : option.movers()) {
            board.move(piece, option.destination());
            observer.onPieceMoved(piece, from, option.route().distance(), blockDirection);
        }
        boolean captured = landingHandler.resolve(option);
        return new TurnOutcome(captured, captured);
    }

    private Direction directionTravelled(Position from) {
        int clockwiseEnd = navigator.move(from.index(), option.route().distance(), Direction.CLOCKWISE);
        return clockwiseEnd == option.destination().index() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
    }
}