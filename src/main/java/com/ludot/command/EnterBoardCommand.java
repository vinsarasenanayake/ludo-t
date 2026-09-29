package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.port.Coin;
import com.ludot.port.GameEvents;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;

final class EnterBoardCommand extends MoveCommand {

    private final Coin coin;

    EnterBoardCommand(MoveOption option, Board board, Coin coin, Teleporter teleporter, GameEvents.Moves listener) {
        super(option, board, teleporter, listener);
        this.coin = coin;
    }

    @Override
    public void execute() {
        Piece piece = option.leadPiece();
        board.enter(piece, coin.tossHeads() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE);
        listener.onPieceEntered(piece);
        land();
    }

    @Override
    public boolean showsPlayerStatus() {
        return true;
    }
}
