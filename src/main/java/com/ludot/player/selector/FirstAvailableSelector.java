package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;

public class FirstAvailableSelector implements MoveSelector {

    @Override
    public MoveOption select(TurnContext context) {
        return context.options().get(0);
    }
}