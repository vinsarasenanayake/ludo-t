package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.Teleporter;
import com.ludot.random.Coin;
import com.ludot.rules.MoveOption;

public class CommandFactory {

    private final Board board;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final Teleporter teleporter;
    private final MoveEvents listener;

    public CommandFactory(Board board, Coin coin, TrackNavigator navigator,
                          Teleporter teleporter, MoveEvents listener) {
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
