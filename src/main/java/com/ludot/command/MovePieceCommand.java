package com.ludot.command;

import com.ludot.domain.Blockage;
import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.port.MoveListener;
import com.ludot.rules.MoveOption;

public class MovePieceCommand implements GameCommand {

    private final MoveOption option;
    private final Board board;
    private final LandingHandler landingHandler;
    private final MoveListener listener;

    public MovePieceCommand(MoveOption option, Board board, LandingHandler landingHandler, MoveListener listener) {
        this.option = option;
        this.board = board;
        this.landingHandler = landingHandler;
        this.listener = listener;
    }

    @Override
    public TurnOutcome execute() {
        Piece piece = option.leadPiece();
        Position from = piece.position();
        option.route().blockage().ifPresent(blockage -> listener.onPieceBlocked(piece, from, blockage));
        board.move(piece, option.destination());
        reportMove(piece);
        landingHandler.resolve(option);
        return new TurnOutcome(option.capturesAny(), option.capturesAny());
    }

    private void reportMove(Piece piece) {
        if (option.isCutShortByBlock()) {
            listener.onMovedBeforeBlock(piece.colour(), option.destination());
        } else {
            listener.onPieceMoved(piece, option.route(), piece.direction());
        }
    }
}
