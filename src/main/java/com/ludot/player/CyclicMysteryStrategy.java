package com.ludot.player;

import com.ludot.board.Direction;
import com.ludot.rules.MoveOption;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.ludot.board.BoardConstants.PIECES_PER_PLAYER;

/**
 * Blue (2.1.4): one piece is scheduled per round, B1 in one round, B2 in the next and so on. If the
 * scheduled piece cannot move, the next piece in the cycle is used. A counter-clockwise piece prefers
 * any counter-clockwise move onto the mystery cell; a clockwise piece swaps to another move rather
 * than land on the mystery cell.
 */
final class CyclicMysteryStrategy implements PlayerStrategy {

    private int scheduledPieceNumber = 1;

    @Override
    public MoveOption chooseMove(List<MoveOption> options) {
        List<MoveOption> inCycleOrder = options.stream().sorted(byPlaceInCycle()).toList();
        MoveOption scheduled = inCycleOrder.get(0);
        return scheduled.leadPiece().direction() == Direction.COUNTER_CLOCKWISE
                ? counterClockwiseMoveOntoMystery(inCycleOrder).orElse(scheduled)
                : moveAvoidingMystery(scheduled, inCycleOrder);
    }

    @Override
    public void onRoundEnded() {
        scheduledPieceNumber = scheduledPieceNumber % PIECES_PER_PLAYER + 1;
    }

    private Comparator<MoveOption> byPlaceInCycle() {
        return Comparator.comparingInt(
                option -> Math.floorMod(option.leadPiece().number() - scheduledPieceNumber, PIECES_PER_PLAYER));
    }

    private static Optional<MoveOption> counterClockwiseMoveOntoMystery(List<MoveOption> inCycleOrder) {
        return inCycleOrder.stream()
                .filter(MoveOption::landsOnMysteryCell)
                .filter(option -> option.leadPiece().direction() == Direction.COUNTER_CLOCKWISE)
                .findFirst();
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
