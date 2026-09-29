package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.port.GameEvents;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;

final class MovePieceCommand extends MoveCommand {

    MovePieceCommand(MoveOption option, Board board, Teleporter teleporter, GameEvents.Moves listener) {
        super(option, board, teleporter, listener);
    }

    @Override
    public void execute() {
        Piece piece = option.leadPiece();
        Position from = piece.position();
        option.route().blockage().ifPresent(blockage -> listener.onPieceBlocked(piece, from, blockage));
        board.move(piece, option.destination());
        if (option.isCutShortByBlock()) {
            listener.onMovedBeforeBlock(piece.colour(), option.destination());
        } else {
            listener.onPieceMoved(piece, option.route(), piece.direction());
        }
        land();
    }
}
