package com.ludot.rules;

import com.ludot.domain.Piece;
import com.ludot.domain.Position;

import java.util.List;

public record MoveOption(MoveType type, List<Piece> movers, Route route, Landing landing, boolean leavesBlock) {

    public Piece leadPiece() {
        return movers.get(0);
    }

    public Position destination() {
        return route.destination();
    }

    public boolean capturesAny() {
        return landing.capturesAny();
    }

    public boolean formsBlock() {
        return landing.formsBlock();
    }

    public boolean landsOnMysteryCell() {
        return landing.onMysteryCell();
    }

    public boolean isCutShortByBlock() {
        return route.isCutShortByBlock();
    }
}