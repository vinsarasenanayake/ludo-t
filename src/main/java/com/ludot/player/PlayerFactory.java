package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.rules.TrackNavigator;

public class PlayerFactory {

    private final TrackNavigator navigator;

    public PlayerFactory(TrackNavigator navigator) {
        this.navigator = navigator;
    }

    public Player createPlayer(Colour colour) {
        return new Player(colour, createStrategy(colour));
    }

    private PlayerStrategy createStrategy(Colour colour) {
        return switch (colour) {
            case RED -> new AggressiveCaptureStrategy(navigator);
            case GREEN -> new BlockingStrategy(navigator);
            case YELLOW -> new WinningStrategy(navigator);
            case BLUE -> new CyclicMysteryStrategy();
        };
    }
}
