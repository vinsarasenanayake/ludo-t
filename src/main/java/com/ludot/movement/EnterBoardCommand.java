package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.mystery.Teleporter;
import com.ludot.random.Coin;
import com.ludot.rules.MoveOption;

final class EnterBoardCommand extends MoveCommand {

    private final Coin coin;

    EnterBoardCommand(MoveOption option, Board board, Coin coin, Teleporter teleporter, MoveEvents listener) {
        super(option, board, teleporter, listener);
        this.coin = coin;
    }

    @Override
    public void execute() {
        Piece piece = option().leadPiece();
        board().enter(piece, coin.tossHeads() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE);
        listener().onPieceEntered(piece);
        land();
    }

    @Override
    public boolean showsPlayerStatus() {
        return true;
    }
}
