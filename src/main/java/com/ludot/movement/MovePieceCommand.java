package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.mystery.Teleporter;
import com.ludot.rules.MoveOption;

final class MovePieceCommand extends MoveCommand {

    MovePieceCommand(MoveOption option, Board board, Teleporter teleporter, MoveEvents listener) {
        super(option, board, teleporter, listener);
    }

    @Override
    public void execute() {
        Piece piece = option().leadPiece();
        Position from = piece.position();
        option().route().blockage().ifPresent(blockage -> listener().onPieceBlocked(piece, from, blockage));
        board().move(piece, option().destination());
        if (option().isCutShortByBlock()) {
            listener().onMovedBeforeBlock(piece.colour(), option().destination());
        } else {
            listener().onPieceMoved(piece, option().route(), piece.direction());
        }
        land();
    }
}
