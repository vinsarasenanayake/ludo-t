package com.ludot;

import com.ludot.board.Colour;
import com.ludot.game.GameResultDto;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GameConfigurationTest {

    // Design: the same seed always plays exactly the same game, so a game can be checked by hand
    @Test
    void sameSeedPlaysTheSameGame() {
        assertEquals(playWithSeed(42L), playWithSeed(42L));
    }

    // Design: a different seed gives a different game
    @Test
    void differentSeedPlaysADifferentGame() {
        assertNotEquals(playWithSeed(42L), playWithSeed(7L));
    }

    // R11 + Brief 3.1: the default seed 42 always plays the same game, which Green wins after 167 rounds
    @Test
    void defaultSeedGameIsWonByGreen() {
        GameResultDto result = playWithSeed(42L);
        assertEquals(List.of(Colour.GREEN, Colour.YELLOW, Colour.RED, Colour.BLUE), result.finishingOrder());
        assertEquals(167, result.rounds());
        assertFalse(result.stalled());
    }

    private static GameResultDto playWithSeed(long seed) {
        PrintStream silentConsole = new PrintStream(new ByteArrayOutputStream());
        return new GameConfiguration(seed, silentConsole).createGame().play();
    }
}
