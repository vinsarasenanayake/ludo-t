package com.ludot.player.selector;

import com.ludot.rules.MoveOption;

import java.util.Optional;

// Chain of Responsibility: each rule either picks a move or passes the turn to the next rule.
// select() is the fixed Template Method; subclasses only fill in trySelect().
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
