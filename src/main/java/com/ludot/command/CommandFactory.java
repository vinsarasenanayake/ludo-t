package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.port.Coin;
import com.ludot.port.MoveListener;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

// Factory Method (module definition): all command creation happens here, chosen by the move type.
public class CommandFactory {

    private final Board board;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final LandingHandler landingHandler;
    private final MoveListener listener;

    public CommandFactory(Board board, Coin coin, TrackNavigator navigator,
                          LandingHandler landingHandler, MoveListener listener) {
        this.board = board;
        this.coin = coin;
        this.navigator = navigator;
        this.landingHandler = landingHandler;
        this.listener = listener;
    }

    public GameCommand create(MoveOption option) {
        return switch (option.type()) {
            case ENTER_BOARD -> new EnterBoardCommand(option, board, coin, landingHandler, listener);
            case MOVE_PIECE -> new MovePieceCommand(option, board, landingHandler, listener);
            case MOVE_BLOCK -> new MoveBlockCommand(option, board, navigator, landingHandler, listener);
        };
    }

    public GameCommand createNoMove(Colour colour) {
        return new NullMoveCommand(colour, listener);
    }
}
