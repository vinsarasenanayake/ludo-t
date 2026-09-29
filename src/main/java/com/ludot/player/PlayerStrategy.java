package com.ludot.player;

import com.ludot.rules.MoveOption;

public interface PlayerStrategy {

    MoveOption chooseMove(TurnContext context);
}