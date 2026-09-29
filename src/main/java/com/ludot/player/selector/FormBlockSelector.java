package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;

import java.util.Optional;

public class FormBlockSelector extends ChainedMoveSelector {

    public FormBlockSelector(MoveSelector next) {
        super(next);
    }

    @Override
    protected Optional<MoveOption> trySelect(TurnContext context) {
        return context.options().stream()
                .filter(MoveOption::formsBlock)
                .findFirst();
    }
}