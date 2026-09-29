package com.ludot.player;

import com.ludot.domain.Direction;
import com.ludot.player.selector.TurnContext;
import com.ludot.rules.MoveOption;

import java.util.List;

import static com.ludot.domain.BoardConstants.PIECES_PER_PLAYER;

// Blue (section 2.1.4): tries B1, B2, B3, B4 in turn, skipping pieces that cannot move.
// Counter-clockwise pieces aim for the mystery cell; clockwise pieces avoid it.
public class CyclicMysteryStrategy implements PlayerStrategy {

    private int nextPieceNumber = 1;

    @Override
    public MoveOption chooseMove(TurnContext context) {
        for (int skipped = 0; skipped < PIECES_PER_PLAYER; skipped++) {
            int pieceNumber = wrapToPieceNumber(nextPieceNumber + skipped);
            List<MoveOption> pieceOptions = optionsFor(pieceNumber, context.options());
            if (!pieceOptions.isEmpty()) {
                nextPieceNumber = wrapToPieceNumber(pieceNumber + 1);
                return preferredByMysteryRule(pieceOptions);
            }
        }
        return context.options().get(0);
    }

    public int nextPieceNumber() {
        return nextPieceNumber;
    }

    // Keeps the count cycling 1, 2, 3, 4, 1, ... (for example 5 becomes 1).
    private static int wrapToPieceNumber(int number) {
        return (number - 1) % PIECES_PER_PLAYER + 1;
    }

    private List<MoveOption> optionsFor(int pieceNumber, List<MoveOption> options) {
        return options.stream()
                .filter(option -> option.leadPiece().number() == pieceNumber)
                .toList();
    }

    private MoveOption preferredByMysteryRule(List<MoveOption> pieceOptions) {
        boolean seeksMystery = pieceOptions.get(0).leadPiece().direction() == Direction.COUNTER_CLOCKWISE;
        return pieceOptions.stream()
                .filter(option -> option.landsOnMysteryCell() == seeksMystery)
                .findFirst()
                .orElse(pieceOptions.get(0));
    }
}
