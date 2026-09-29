package com.ludot.port;

// Everything the game reports. Each part of the engine depends only on the small
// listener it needs (ISP); the console reporter implements them all.
public interface GameObserver extends TurnOrderListener, GameProgressListener, TurnListener,
        MoveListener, MysteryListener {
}
