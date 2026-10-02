package com.ludot.movement;

import com.ludot.board.Piece;
import com.ludot.rules.MoveOption;

// T-3: every move is blocked, so the throw is ignored
final class BlockedThrowCommand implements GameCommand {

    private final MoveOption option;
    private final MoveEvents listener;

    BlockedThrowCommand(MoveOption option, MoveEvents listener) {
        this.option = option;
        this.listener = listener;
    }

    @Override
    public void execute() {
        for (Piece piece : option.movers()) {
            option.route().blockage()
                    .ifPresent(blockage -> listener.onPieceBlocked(piece, piece.position(), blockage));
        }
        listener.onBlockedThrowIgnored(option.leadPiece().colour());
    }

    @Override
    public boolean grantsBonusRoll() {
        return false;
    }

    @Override
    public boolean showsPlayerStatus() {
        return false;
    }

    @Override
    public boolean endsTurn() {
        return true;
    }
}