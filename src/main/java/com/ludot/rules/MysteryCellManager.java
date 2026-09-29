package com.ludot.rules;

import com.ludot.domain.ActiveMysteryCell;
import com.ludot.domain.Board;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.NoMysteryCell;
import com.ludot.port.CellPicker;
import com.ludot.port.MysteryListener;

import java.util.ArrayList;
import java.util.List;

import static com.ludot.domain.BoardConstants.MYSTERY_LIFETIME_ROUNDS;
import static com.ludot.domain.BoardConstants.MYSTERY_SPAWN_DELAY_ROUNDS;

// Rule T-10: appears two rounds after pieces reach the track, stays four rounds,
// then moves to a random empty cell that is never the same cell twice in a row.
public class MysteryCellManager {

    private static final int NO_PREVIOUS_LOCATION = -1;

    private final Board board;
    private final CellPicker cellPicker;
    private final MysteryListener listener;
    private MysteryCell current = NoMysteryCell.INSTANCE;
    private boolean countingStarted;
    private int roundsCounted;
    private int previousLocation = NO_PREVIOUS_LOCATION;

    public MysteryCellManager(Board board, CellPicker cellPicker, MysteryListener listener) {
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
            return;
        }
        countTowardsFirstSpawn(anyPieceOnTrack);
    }

    private void countTowardsFirstSpawn(boolean anyPieceOnTrack) {
        if (!countingStarted) {
            countingStarted = anyPieceOnTrack;
            return;
        }
        roundsCounted++;
        if (roundsCounted >= MYSTERY_SPAWN_DELAY_ROUNDS) {
            spawn();
        }
    }

    private void spawn() {
        List<Integer> candidates = new ArrayList<>(board.emptyCellIndexes());
        candidates.remove(Integer.valueOf(previousLocation));
        if (candidates.isEmpty()) {
            current = NoMysteryCell.INSTANCE;
            return;
        }
        int location = cellPicker.pick(candidates);
        previousLocation = location;
        current = new ActiveMysteryCell(location, MYSTERY_LIFETIME_ROUNDS);
        listener.onMysteryCellSpawned(current);
    }
}
