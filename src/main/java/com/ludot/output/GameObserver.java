package com.ludot.output;

import com.ludot.game.GameEvents;
import com.ludot.movement.MoveEvents;
import com.ludot.mystery.MysteryEvents;

// Joins every event interface for the reporter
public interface GameObserver extends GameEvents.TurnOrder, GameEvents.Rounds, GameEvents.Turns,
        MoveEvents, MysteryEvents {
}