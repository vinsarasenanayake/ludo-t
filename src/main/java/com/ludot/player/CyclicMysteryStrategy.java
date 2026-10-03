package com.ludot.player;

import com.ludot.board.Direction;
import com.ludot.rules.MoveOption;

import java.util.Comparator;
import java.util.List;

import static com.ludot.board.BoardConstants.PIECES_PER_PLAYER;

// Blue: one piece per round
final class CyclicMysteryStrategy implements PlayerStrategy {

    private int scheduledPieceNumber = 1;

    @Override
    public MoveOption chooseMove(List<MoveOption> options) {
        List<MoveOption> inCycleOrder = options.stream().sorted(byPlaceInCycle()).toList();
        MoveOption scheduled = inCycleOrder.getFirst();
        // The scheduled piece's direction decides: seek or avoid the mystery cell
        return scheduled.leadPiece().direction() == Direction.COUNTER_CLOCKWISE
                ? scheduledMoveOntoMystery(scheduled, inCycleOrder)
                : moveAvoidingMystery(scheduled, inCycleOrder);
    }

    @Override
    public void onRoundEnded() {
        scheduledPieceNumber = scheduledPieceNumber % PIECES_PER_PLAYER + 1;
    }

    // The scheduled piece first, then the rest in cycle order
    private Comparator<MoveOption> byPlaceInCycle() {
        return Comparator.comparingInt(
                option -> Math.floorMod(option.leadPiece().number() - scheduledPieceNumber, PIECES_PER_PLAYER));
    }

    // Only the scheduled piece's own moves are considered
    private static MoveOption scheduledMoveOntoMystery(MoveOption scheduled, List<MoveOption> inCycleOrder) {
        return inCycleOrder.stream()
                .filter(option -> option.leadPiece() == scheduled.leadPiece())
                .filter(MoveOption::landsOnMysteryCell)
                .findFirst()
                .orElse(scheduled);
    }

    private static MoveOption moveAvoidingMystery(MoveOption scheduled, List<MoveOption> inCycleOrder) {
        if (!scheduled.landsOnMysteryCell()) {
            return scheduled;
        }
        return inCycleOrder.stream()
                .filter(option -> !option.landsOnMysteryCell())
                .findFirst()
                .orElse(scheduled);
    }
}