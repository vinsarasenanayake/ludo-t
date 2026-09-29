package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;

import java.util.Optional;

public abstract class ChainedMoveSelector implements MoveSelector {

    private final MoveSelector next;

    protected ChainedMoveSelector(MoveSelector next) {
        this.next = next;
    }

    @Override
    public final MoveOption select(TurnContext context) {
        return trySelect(context).orElseGet(() -> next.select(context));
    }

    protected abstract Optional<MoveOption> trySelect(TurnContext context);
}