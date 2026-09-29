package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.port.GameObserver;
import com.ludot.rules.Blockage;
import com.ludot.rules.MoveOption;

public class MovePieceCommand implements GameCommand {

    private final MoveOption option;
    private final Board board;
    private final LandingHandler landingHandler;
    private final GameObserver observer;

    public MovePieceCommand(MoveOption option, Board board, LandingHandler landingHandler, GameObserver observer) {
        this.option = option;
        this.board = board;
        this.landingHandler = landingHandler;
        this.observer = observer;
    }

    @Override
    public TurnOutcome execute() {
        Piece piece = option.leadPiece();
        Position from = piece.position();
        option.route().blockage().ifPresent(blockage -> reportBlockage(piece, from, blockage));
        board.move(piece, option.destination());
        reportMove(piece, from);
        boolean captured = landingHandler.resolve(option);
        return new TurnOutcome(captured, captured);
    }

    private void reportBlockage(Piece piece, Position from, Blockage blockage) {
        observer.onPieceBlocked(piece, from, blockage.intendedDestination(), blockage.blockingPiece());
    }

    private void reportMove(Piece piece, Position from) {
        if (option.isCutShortByBlock()) {
            observer.onMovedBeforeBlock(piece.colour(), option.destination());
        } else {
            observer.onPieceMoved(piece, from, option.route().distance(), piece.direction());
        }
    }
}