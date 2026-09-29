package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;

import java.util.Optional;

public class CaptureByPieceNeedingCaptureSelector extends ChainedMoveSelector {

    public CaptureByPieceNeedingCaptureSelector(MoveSelector next) {
        super(next);
    }

    @Override
    protected Optional<MoveOption> trySelect(TurnContext context) {
        return context.options().stream()
                .filter(MoveOption::capturesAny)
                .filter(option -> !option.leadPiece().hasCaptured())
                .findFirst();
    }
}