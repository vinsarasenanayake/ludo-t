package com.ludot.player;

import com.ludot.player.selector.TurnContext;
import com.ludot.rules.MoveOption;

public interface PlayerStrategy {

    MoveOption chooseMove(TurnContext context);
}
