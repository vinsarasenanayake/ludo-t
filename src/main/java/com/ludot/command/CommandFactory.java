package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.port.Coin;
import com.ludot.port.GameEvents;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;

public class CommandFactory {

    private final Board board;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final Teleporter teleporter;
    private final GameEvents.Moves listener;

    public CommandFactory(Board board, Coin coin, TrackNavigator navigator,
                          Teleporter teleporter, GameEvents.Moves listener) {
        this.board = board;
        this.coin = coin;
        this.navigator = navigator;
        this.teleporter = teleporter;
        this.listener = listener;
    }

    public GameCommand create(MoveOption option) {
        return switch (option.type()) {
            case ENTER_BOARD -> new EnterBoardCommand(option, board, coin, teleporter, listener);
            case MOVE_PIECE -> new MovePieceCommand(option, board, teleporter, listener);
            case MOVE_BLOCK -> new MoveBlockCommand(option, board, navigator, teleporter, listener);
        };
    }

    public GameCommand createNoMove(Colour colour) {
        return new NullMoveCommand(colour, listener);
    }
}
