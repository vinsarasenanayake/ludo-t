package com.ludot.rules;

import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;

import java.util.List;

// One legal move: who moves, the route, and the landing
public record MoveOption(Type type, List<Piece> movers, Route route, Landing landing, boolean leavesBlock) {

    public enum Type { ENTER_BOARD, MOVE_PIECE, MOVE_BLOCK }

    // What happens where the move lands
    public record Landing(List<Piece> victims, boolean formsBlock, boolean onMysteryCell) {

        public Landing {
            victims = List.copyOf(victims);
        }

        public static Landing offTrack() {
            return new Landing(List.of(), false, false);
        }
    }

    public MoveOption {
        movers = List.copyOf(movers);
    }

    public Piece leadPiece() {
        return movers.getFirst();
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

    // T-3: blocked without moving a single cell
    public boolean isFullyBlocked() {
        return isCutShortByBlock() && route.distance() == 0;
    }
}