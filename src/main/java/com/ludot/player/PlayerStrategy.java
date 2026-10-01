package com.ludot.player;

import com.ludot.rules.MoveOption;

import java.util.List;

interface PlayerStrategy {

    MoveOption chooseMove(List<MoveOption> options);
}
