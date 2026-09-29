package com.ludot.player;

import com.ludot.domain.Direction;
import com.ludot.rules.MoveOption;

import java.util.List;

import static com.ludot.domain.BoardConstants.PIECES_PER_PLAYER;

public class CyclicMysteryStrategy implements PlayerStrategy {

    private int nextPieceNumber = 1;

    @Override
    public MoveOption chooseMove(TurnContext context) {
        for (int offset = 0; offset < PIECES_PER_PLAYER; offset++) {
            int pieceNumber = (nextPieceNumber - 1 + offset) % PIECES_PER_PLAYER + 1;
            List<MoveOption> pieceOptions = optionsFor(pieceNumber, context.options());
            if (!pieceOptions.isEmpty()) {
                nextPieceNumber = pieceNumber % PIECES_PER_PLAYER + 1;
                return preferredByMysteryRule(pieceOptions);
            }
        }
        return context.options().get(0);
    }

    public int nextPieceNumber() {
        return nextPieceNumber;
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