package com.ludot.player;

import com.ludot.domain.MysteryCell;
import com.ludot.rules.MoveOption;

import java.util.List;

public record TurnContext(int roll, List<MoveOption> options, MysteryCell mysteryCell) {

    public TurnContext {
        options = List.copyOf(options);
    }
}