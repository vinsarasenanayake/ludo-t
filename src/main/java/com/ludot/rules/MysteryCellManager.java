package com.ludot.rules;

import com.ludot.domain.ActiveMysteryCell;
import com.ludot.domain.Board;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.NoMysteryCell;
import com.ludot.port.CellPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ludot.domain.BoardConstants.MYSTERY_LIFETIME_ROUNDS;
import static com.ludot.domain.BoardConstants.MYSTERY_SPAWN_DELAY_ROUNDS;

public class MysteryCellManager {

    private static final int NO_PREVIOUS_LOCATION = -1;

    private final Board board;
    private final CellPicker cellPicker;
    private MysteryCell current = NoMysteryCell.INSTANCE;
    private boolean countingStarted;
    private int roundsCounted;
    private int previousLocation = NO_PREVIOUS_LOCATION;

    public MysteryCellManager(Board board, CellPicker cellPicker) {
        this.board = board;
        this.cellPicker = cellPicker;
    }

    public MysteryCell current() {
        return current;
    }

    public Optional<MysteryCell> endRound(boolean anyPieceOnTrack) {
        if (current.isActive()) {
            current = current.afterOneRound();
            return current.roundsRemaining() == 0 ? spawn() : Optional.empty();
        }
        return countTowardsFirstSpawn(anyPieceOnTrack);
    }

    private Optional<MysteryCell> countTowardsFirstSpawn(boolean anyPieceOnTrack) {
        if (!countingStarted) {
            countingStarted = anyPieceOnTrack;
            return Optional.empty();
        }
        roundsCounted++;
        return roundsCounted >= MYSTERY_SPAWN_DELAY_ROUNDS ? spawn() : Optional.empty();
    }

    private Optional<MysteryCell> spawn() {
        List<Integer> candidates = new ArrayList<>(board.emptyCellIndexes());
        candidates.remove(Integer.valueOf(previousLocation));
        if (candidates.isEmpty()) {
            current = NoMysteryCell.INSTANCE;
            return Optional.empty();
        }
        int location = cellPicker.pick(candidates);
        previousLocation = location;
        current = new ActiveMysteryCell(location, MYSTERY_LIFETIME_ROUNDS);
        return Optional.of(current);
    }
}