package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Cell;
import com.ludot.domain.Colour;
import com.ludot.domain.Piece;

import java.util.List;

public class CaptureResolver {

    private final Board board;

    public CaptureResolver(Board board) {
        this.board = board;
    }

    // Rule 6 for single pieces. Rule T-8: a block can only be captured by a block of the same size.
    public List<Piece> findVictims(Colour moverColour, int cellIndex, int movingGroupSize) {
        Cell cell = board.cellAt(cellIndex);
        List<Piece> opponents = cell.opponentsOf(moverColour);
        if (!cell.isBlock()) {
            return opponents;
        }
        boolean sameSizeBlock = opponents.size() == movingGroupSize;
        return sameSizeBlock ? opponents : List.of();
    }

    public void capture(List<Piece> attackers, List<Piece> victims) {
        for (Piece victim : victims) {
            board.sendToBase(victim);
        }
        for (Piece attacker : attackers) {
            attacker.recordCapture();
        }
    }
}
