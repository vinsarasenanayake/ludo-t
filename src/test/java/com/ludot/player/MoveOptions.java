package com.ludot.player;

import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveOption.Landing;
import com.ludot.rules.MoveOption.Type;

import java.util.List;

final class MoveOptions {

    private static final Landing PLAIN = new Landing(List.of(), false, false);
    private static final Landing ONTO_OWN_PIECE = new Landing(List.of(), true, false);
    private static final Landing ONTO_MYSTERY = new Landing(List.of(), false, true);

    private MoveOptions() {
    }

    static MoveOption enter(Piece piece) {
        return entry(piece, PLAIN);
    }

    static MoveOption enterFormingBlock(Piece piece) {
        return entry(piece, ONTO_OWN_PIECE);
    }

    static MoveOption move(Piece piece) {
        return single(piece, PLAIN, false);
    }

    static MoveOption capture(Piece piece, Piece victim) {
        return single(piece, new Landing(List.of(victim), false, false), false);
    }

    static MoveOption formingBlock(Piece piece) {
        return single(piece, ONTO_OWN_PIECE, false);
    }

    static MoveOption movingBlockToBlock(Piece piece) {
        return single(piece, ONTO_OWN_PIECE, true);
    }

    static MoveOption leavingBlock(Piece piece) {
        return single(piece, PLAIN, true);
    }

    static MoveOption ontoMystery(Piece piece) {
        return single(piece, ONTO_MYSTERY, false);
    }

    static MoveOption blockMove(Piece first, Piece second) {
        return new MoveOption(Type.MOVE_BLOCK, List.of(first, second), stay(first), PLAIN, false);
    }

    private static MoveOption entry(Piece piece, Landing landing) {
        Route route = Route.completed(Position.base(), Position.onTrack(piece.colour().startCell()), 0, 0);
        return new MoveOption(Type.ENTER_BOARD, List.of(piece), route, landing, false);
    }

    private static MoveOption single(Piece piece, Landing landing, boolean leavesBlock) {
        return new MoveOption(Type.MOVE_PIECE, List.of(piece), stay(piece), landing, leavesBlock);
    }

    private static Route stay(Piece piece) {
        return Route.completed(piece.position(), piece.position(), 1, 0);
    }
}
