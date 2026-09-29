package com.ludot.player.selector;

import com.ludot.rules.MoveOption;

// The last link of every chain, so a move is always chosen and the chain never ends in null.
public class FirstAvailableSelector implements MoveSelector {

    @Override
    public MoveOption select(TurnContext context) {
        return context.options().get(0);
    }
}
