package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;

import java.util.List;

public class MovePlanner {

    private final Board board;
    private final TrackNavigator navigator;
    private final CaptureResolver captureResolver;

    public MovePlanner(Board board, TrackNavigator navigator, CaptureResolver captureResolver) {
        this.board = board;
        this.navigator = navigator;
        this.captureResolver = captureResolver;
    }

    public List<MoveOption> findOptions(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        return List.of();
    }
}