package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.port.Coin;
import com.ludot.port.GameObserver;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

public class CommandFactory {

    private final Board board;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final LandingHandler landingHandler;
    private final GameObserver observer;

    public CommandFactory(Board board, Coin coin, TrackNavigator navigator,
                          LandingHandler landingHandler, GameObserver observer) {
        this.board = board;
        this.coin = coin;
        this.navigator = navigator;
        this.landingHandler = landingHandler;
        this.observer = observer;
    }

    public GameCommand create(MoveOption option) {
        return switch (option.type()) {
            case ENTER_BOARD -> new EnterBoardCommand(option, board, coin, landingHandler, observer);
            case MOVE_PIECE -> new MovePieceCommand(option, board, landingHandler, observer);
            case MOVE_BLOCK -> new MoveBlockCommand(option, board, navigator, landingHandler, observer);
        };
    }

    public GameCommand createNoMove(Colour colour) {
        return new NullMoveCommand(colour, observer);
    }
}