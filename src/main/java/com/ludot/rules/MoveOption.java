package com.ludot.rules;

import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;

import java.util.List;

public record MoveOption(Type type, List<Piece> movers, Route route, Landing landing, boolean leavesBlock) {

    public enum Type { ENTER_BOARD, MOVE_PIECE, MOVE_BLOCK }

    public record Landing(List<Piece> victims, boolean formsBlock, boolean onMysteryCell) {

        public static Landing offTrack() {
            return new Landing(List.of(), false, false);
        }
    }

    public Piece leadPiece() {
        return movers.get(0);
    }

    public Position destination() {
        return route.destination();
    }

    public boolean capturesAny() {
        return !landing.victims().isEmpty();
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
