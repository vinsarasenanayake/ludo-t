package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.port.Coin;
import com.ludot.port.GameObserver;
import com.ludot.rules.MoveOption;

public class EnterBoardCommand implements GameCommand {

    private final MoveOption option;
    private final Board board;
    private final Coin coin;
    private final LandingHandler landingHandler;
    private final GameObserver observer;

    public EnterBoardCommand(MoveOption option, Board board, Coin coin,
                             LandingHandler landingHandler, GameObserver observer) {
        this.option = option;
        this.board = board;
        this.coin = coin;
        this.landingHandler = landingHandler;
        this.observer = observer;
    }

    @Override
    public TurnOutcome execute() {
        Piece piece = option.leadPiece();
        Direction direction = coin.tossHeads() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
        board.enter(piece, direction);
        observer.onPieceEntered(piece);
        boolean captured = landingHandler.resolve(option);
        return new TurnOutcome(captured, true);
    }
}