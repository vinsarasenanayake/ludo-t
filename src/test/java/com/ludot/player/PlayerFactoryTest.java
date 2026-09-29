package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.rules.TrackNavigator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerFactoryTest {

    private final PlayerFactory factory = new PlayerFactory(new TrackNavigator());

    @Test
    void createsAPlayerOfTheRequestedColour() {
        assertEquals(Colour.GREEN, factory.createPlayer(Colour.GREEN).colour());
    }

    @Test
    void everyColourCanBeCreated() {
        for (Colour colour : Colour.values()) {
            assertEquals(4, factory.createPlayer(colour).piecesInBase());
        }
    }
}