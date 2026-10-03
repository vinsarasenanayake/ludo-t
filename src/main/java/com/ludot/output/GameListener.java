package com.ludot.output;

import com.ludot.game.GameEvents;
import com.ludot.movement.MoveEvents;
import com.ludot.mystery.MysteryEvents;

public interface GameListener extends GameEvents.TurnOrder, GameEvents.Rounds, GameEvents.Turns,
        MoveEvents, MysteryEvents {
}