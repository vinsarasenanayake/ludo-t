package com.ludot.player.selector;

import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveType;

import java.util.Optional;

public class EnterFromBaseSelector extends ChainedMoveSelector {

    public EnterFromBaseSelector(MoveSelector next) {
        super(next);
    }

    @Override
    protected Optional<MoveOption> trySelect(TurnContext context) {
        return context.options().stream()
                .filter(option -> option.type() == MoveType.ENTER_BOARD)
                .findFirst();
    }
}
