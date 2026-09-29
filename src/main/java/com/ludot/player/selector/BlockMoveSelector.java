package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveType;

import java.util.Optional;

public class BlockMoveSelector extends ChainedMoveSelector {

    public BlockMoveSelector(MoveSelector next) {
        super(next);
    }

    @Override
    protected Optional<MoveOption> trySelect(TurnContext context) {
        return context.options().stream()
                .filter(option -> option.type() == MoveType.MOVE_BLOCK)
                .findFirst();
    }
}