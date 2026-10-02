package com.ludot.player;

import com.ludot.rules.MoveOption;

import java.util.List;

// Strategy: how a player picks one move
interface PlayerStrategy {

    MoveOption chooseMove(List<MoveOption> options);

    // Only Blue uses this, to move its cycle on
    default void onRoundEnded() {
    }
}
