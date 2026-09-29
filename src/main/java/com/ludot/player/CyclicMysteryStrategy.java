package com.ludot.player;

import com.ludot.board.Direction;
import com.ludot.rules.MoveOption;

import java.util.List;

import static com.ludot.board.BoardConstants.PIECES_PER_PLAYER;

public class CyclicMysteryStrategy implements PlayerStrategy {

    private int nextPieceNumber = 1;

    @Override
    public MoveOption chooseMove(List<MoveOption> options) {
        for (int skipped = 0; skipped < PIECES_PER_PLAYER; skipped++) {
            int pieceNumber = wrapToPieceNumber(nextPieceNumber + skipped);
            List<MoveOption> pieceOptions = options.stream()
                    .filter(option -> option.leadPiece().number() == pieceNumber)
                    .toList();
            if (!pieceOptions.isEmpty()) {
                nextPieceNumber = wrapToPieceNumber(pieceNumber + 1);
                return preferredByMysteryRule(pieceOptions);
            }
        }
        return options.get(0);
    }

    private static int wrapToPieceNumber(int number) {
        return (number - 1) % PIECES_PER_PLAYER + 1;
    }

    private MoveOption preferredByMysteryRule(List<MoveOption> pieceOptions) {
        boolean seeksMystery = pieceOptions.get(0).leadPiece().direction() == Direction.COUNTER_CLOCKWISE;
        return pieceOptions.stream()
                .filter(option -> option.landsOnMysteryCell() == seeksMystery)
                .findFirst()
                .orElse(pieceOptions.get(0));
    }
}
