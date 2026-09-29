package com.ludot.testsupport;

import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.Route;
import com.ludot.player.selector.TurnContext;
import com.ludot.rules.Landing;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveType;

import java.util.List;

public final class MoveOptions {

    private static final Landing NOTHING_SPECIAL = new Landing(List.of(), false, false);

    private MoveOptions() {
    }

    public static TurnContext context(MoveOption... options) {
        return new TurnContext(List.of(options));
    }

    public static MoveOption enter(Piece piece) {
        Route route = Route.completed(Position.base(), Position.onTrack(piece.colour().startCell()), 0, 0);
        return new MoveOption(MoveType.ENTER_BOARD, List.of(piece), route, NOTHING_SPECIAL, false);
    }

    public static MoveOption move(Piece piece) {
        return new MoveOption(MoveType.MOVE_PIECE, List.of(piece), stay(piece), NOTHING_SPECIAL, false);
    }

    public static MoveOption capture(Piece piece, Piece victim) {
        Landing landing = new Landing(List.of(victim), false, false);
        return new MoveOption(MoveType.MOVE_PIECE, List.of(piece), stay(piece), landing, false);
    }

    public static MoveOption formingBlock(Piece piece) {
        Landing landing = new Landing(List.of(), true, false);
        return new MoveOption(MoveType.MOVE_PIECE, List.of(piece), stay(piece), landing, false);
    }

    public static MoveOption leavingBlock(Piece piece) {
        return new MoveOption(MoveType.MOVE_PIECE, List.of(piece), stay(piece), NOTHING_SPECIAL, true);
    }

    public static MoveOption blockMove(Piece first, Piece second) {
        return new MoveOption(MoveType.MOVE_BLOCK, List.of(first, second), stay(first), NOTHING_SPECIAL, false);
    }

    public static MoveOption ontoMystery(Piece piece) {
        Landing landing = new Landing(List.of(), false, true);
        return new MoveOption(MoveType.MOVE_PIECE, List.of(piece), stay(piece), landing, false);
    }

    private static Route stay(Piece piece) {
        return Route.completed(piece.position(), piece.position(), 1, 0);
    }
}
