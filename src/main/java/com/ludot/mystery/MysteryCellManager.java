package com.ludot.mystery;

import com.ludot.board.Board;
import com.ludot.random.CellPicker;

import java.util.ArrayList;
import java.util.List;

import static com.ludot.board.BoardConstants.MYSTERY_LIFETIME_ROUNDS;
import static com.ludot.board.BoardConstants.MYSTERY_SPAWN_DELAY_ROUNDS;

// T-10: spawns, counts down, and moves the mystery cell
public final class MysteryCellManager {

    private static final int NO_PREVIOUS_LOCATION = -1;

    private final Board board;
    private final CellPicker cellPicker;
    private final MysteryEvents listener;
    private MysteryCell current = MysteryCell.None.INSTANCE;
    private boolean countingStarted;
    private int roundsCounted;
    private int previousLocation = NO_PREVIOUS_LOCATION;

    public MysteryCellManager(Board board, CellPicker cellPicker, MysteryEvents listener) {
        this.board = board;
        this.cellPicker = cellPicker;
        this.listener = listener;
    }

    public MysteryCell current() {
        return current;
    }

    public void endRound(boolean anyPieceOnTrack) {
        if (current.isActive()) {
            current = current.afterOneRound();
            if (current.roundsRemaining() == 0) {
                spawn();
            }
        } else if (!countingStarted) {
            countingStarted = anyPieceOnTrack;
        } else {
            roundsCounted++;
            if (roundsCounted >= MYSTERY_SPAWN_DELAY_ROUNDS) {
                spawn();
            }
        }
    }

    private void spawn() {
        List<Integer> candidates = new ArrayList<>(board.emptyCellIndexes());
        // Never the cell it has just left
        candidates.remove(Integer.valueOf(previousLocation));
        // No empty cell, so no mystery cell this time
        if (candidates.isEmpty()) {
            current = MysteryCell.None.INSTANCE;
            return;
        }
        int location = cellPicker.pick(candidates);
        previousLocation = location;
        current = new MysteryCell.Active(location, MYSTERY_LIFETIME_ROUNDS);
        listener.onMysteryCellSpawned(current);
    }
}
