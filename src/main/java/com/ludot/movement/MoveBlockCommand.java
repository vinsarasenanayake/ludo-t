package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.mystery.Teleporter;
import com.ludot.rules.MoveOption;

final class MoveBlockCommand extends MoveCommand {

    MoveBlockCommand(MoveOption option, Board board, Teleporter teleporter, MoveEvents listener) {
        super(option, board, teleporter, listener);
    }

    @Override
    public void execute() {
        Direction blockDirection = option().leadPiece().direction();
        for (Piece piece : option().movers()) {
            Position from = piece.position();
            option().route().blockage().ifPresent(blockage -> listener().onPieceBlocked(piece, from, blockage));
            board().move(piece, option().destination());
            if (!option().isCutShortByBlock()) {
                listener().onPieceMoved(piece, option().route(), blockDirection);
            }
        }
        if (option().isCutShortByBlock()) {
            listener().onMovedBeforeBlock(option().leadPiece().colour(), option().destination());
        }
        land();
    }
}
