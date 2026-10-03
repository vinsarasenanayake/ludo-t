package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Direction;
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
        Direction travelDirection = option().leadPiece().direction();
        for (Piece piece : option().movers()) {
            Position from = piece.position();
            option().route().blockage().ifPresent(blockage -> listener().onPieceBlocked(piece, from, blockage));
            board().move(piece, option().destination());
            if (!option().isCutShortByBlock()) {
                listener().onPieceMoved(piece, option().route(), travelDirection);
            }
        }
        // T-3: report the stop before the block
        if (option().isCutShortByBlock()) {
            listener().onMovedBeforeBlock(option().leadPiece().colour(), option().destination());
        }
        land();
    }
}