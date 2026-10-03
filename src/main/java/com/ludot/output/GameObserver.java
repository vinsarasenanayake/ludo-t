package com.ludot.output;

import com.ludot.game.GameEvents;
import com.ludot.movement.MoveEvents;
import com.ludot.mystery.MysteryEvents;

public interface GameObserver extends GameEvents.TurnOrder, GameEvents.Rounds, GameEvents.Turns,
        MoveEvents, MysteryEvents {
}