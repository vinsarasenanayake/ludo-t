package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.port.Coin;
import com.ludot.port.MoveListener;
import com.ludot.rules.MoveOption;

public class EnterBoardCommand implements GameCommand {

    private final MoveOption option;
    private final Board board;
    private final Coin coin;
    private final LandingHandler landingHandler;
    private final MoveListener listener;

    public EnterBoardCommand(MoveOption option, Board board, Coin coin,
                             LandingHandler landingHandler, MoveListener listener) {
        this.option = option;
        this.board = board;
        this.coin = coin;
        this.landingHandler = landingHandler;
        this.listener = listener;
    }

    @Override
    public TurnOutcome execute() {
        Piece piece = option.leadPiece();
        // Rule T-1: a coin toss at X decides the direction (heads = clockwise).
        Direction direction = coin.tossHeads() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
        board.enter(piece, direction);
        listener.onPieceEntered(piece);
        landingHandler.resolve(option);
        return new TurnOutcome(option.capturesAny(), true);
    }
}
