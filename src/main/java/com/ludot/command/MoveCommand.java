package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.port.GameEvents;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;

abstract class MoveCommand implements GameCommand {

    protected final MoveOption option;
    protected final Board board;
    protected final GameEvents.Moves listener;
    private final Teleporter teleporter;

    MoveCommand(MoveOption option, Board board, Teleporter teleporter, GameEvents.Moves listener) {
        this.option = option;
        this.board = board;
        this.teleporter = teleporter;
        this.listener = listener;
    }

    @Override
    public boolean grantsBonusRoll() {
        return option.capturesAny();
    }

    @Override
    public boolean showsPlayerStatus() {
        return option.capturesAny();
    }

    protected void land() {
        for (Piece piece : option.movers()) {
            for (int pass = 0; pass < option.route().approachPassesGained(); pass++) {
                piece.recordApproachPass();
            }
        }
        if (option.capturesAny()) {
            captureVictims();
        }
        if (option.landsOnMysteryCell()) {
            option.movers().forEach(teleporter::teleport);
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
