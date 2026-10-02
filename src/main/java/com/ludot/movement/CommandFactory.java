package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.mystery.Teleporter;
import com.ludot.random.Coin;
import com.ludot.rules.MoveOption;

// Factory Method: picks the right command for a move option
public final class CommandFactory {

    private final Board board;
    private final Coin coin;
    private final Teleporter teleporter;
    private final MoveEvents listener;

    public CommandFactory(Board board, Coin coin, Teleporter teleporter, MoveEvents listener) {
        this.board = board;
        this.coin = coin;
        this.teleporter = teleporter;
        this.listener = listener;
    }

    public GameCommand create(MoveOption option) {
        if (option.isFullyBlocked()) {
            return new BlockedThrowCommand(option, listener);
        }
        return switch (option.type()) {
            case ENTER_BOARD -> new EnterBoardCommand(option, board, coin, teleporter, listener);
            case MOVE_PIECE, MOVE_BLOCK -> new MovePieceCommand(option, board, teleporter, listener);
        };
    }

    public GameCommand createNoMove() {
        return new NullMoveCommand();
    }
}
