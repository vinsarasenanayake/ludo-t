package com.ludot.port;

public interface GameObserver extends GameEvents.TurnOrder, GameEvents.Rounds, GameEvents.Turns,
        GameEvents.Moves, GameEvents.Mystery {
}
