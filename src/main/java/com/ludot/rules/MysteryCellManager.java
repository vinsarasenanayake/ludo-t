package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.MysteryCell;
import com.ludot.port.CellPicker;
import com.ludot.port.GameEvents;

import java.util.ArrayList;
import java.util.List;

import static com.ludot.domain.BoardConstants.MYSTERY_LIFETIME_ROUNDS;
import static com.ludot.domain.BoardConstants.MYSTERY_SPAWN_DELAY_ROUNDS;

public class MysteryCellManager {

    private static final int NO_PREVIOUS_LOCATION = -1;

    private final Board board;
    private final CellPicker cellPicker;
    private final GameEvents.Mystery listener;
    private MysteryCell current = MysteryCell.None.INSTANCE;
    private boolean countingStarted;
    private int roundsCounted;
    private int previousLocation = NO_PREVIOUS_LOCATION;

    public MysteryCellManager(Board board, CellPicker cellPicker, GameEvents.Mystery listener) {
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
        } else if (++roundsCounted >= MYSTERY_SPAWN_DELAY_ROUNDS) {
            spawn();
        }
    }

    private void spawn() {
        List<Integer> candidates = new ArrayList<>(board.emptyCellIndexes());
        candidates.remove(Integer.valueOf(previousLocation));
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
