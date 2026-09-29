package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;

public interface MoveSelector {

    MoveOption select(TurnContext context);
}