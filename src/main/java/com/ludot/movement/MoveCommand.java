package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.mystery.Teleporter;
import com.ludot.rules.MoveOption;

abstract class MoveCommand implements GameCommand {

    private final MoveOption option;
    private final Board board;
    private final MoveEvents listener;
    private final Teleporter teleporter;

    MoveCommand(MoveOption option, Board board, Teleporter teleporter, MoveEvents listener) {
        this.option = option;
        this.board = board;
        this.teleporter = teleporter;
        this.listener = listener;
    }

    protected MoveOption option() {
        return option;
    }

    protected Board board() {
        return board;
    }

    protected MoveEvents listener() {
        return listener;
    }

    @Override
    public boolean grantsBonusRoll() {
        return option.capturesAny();
    }

    @Override
    public boolean showsPlayerStatus() {
        return grantsBonusRoll();
    }

    protected void land() {
        Direction travelDirection = option.leadPiece().direction();
        for (Piece piece : option.movers()) {
            if (piece.direction() == travelDirection) {
                recordApproachPasses(piece);
            }
        }
        if (option.capturesAny()) {
            captureVictims();
        }
        if (option.landsOnMysteryCell()) {
            option.movers().forEach(teleporter::teleport);
        }
    }

    private void recordApproachPasses(Piece piece) {
        for (int pass = 0; pass < option.route().approachPassesGained(); pass++) {
            piece.recordApproachPass();
        }
    }

    private void captureVictims() {
        for (Piece victim : option.landing().victims()) {
            listener.onCapture(option.leadPiece(), victim, option.destination());
        }
        option.landing().victims().forEach(board::sendToBase);
        option.movers().forEach(Piece::recordCapture);
    }
}
