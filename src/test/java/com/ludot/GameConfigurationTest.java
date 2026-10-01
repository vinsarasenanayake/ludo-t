package com.ludot;

import com.ludot.board.Colour;
import com.ludot.game.GameResultDto;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameConfigurationTest {

    // Design: the same seed always plays exactly the same game, so a game can be checked by hand
    @Test
    void sameSeedPlaysTheSameGame() {
        assertEquals(playWithSeed(42L), playWithSeed(42L));
    }

    // Regression snapshot: the default seed 42 always plays the same game, which Green wins after 220 rounds
    @Test
    void defaultSeedGameMatchesItsRecordedResult() {
        GameResultDto result = playWithSeed(42L);
        assertEquals(List.of(Colour.GREEN, Colour.YELLOW, Colour.BLUE, Colour.RED), result.finishingOrder());
        assertEquals(220, result.rounds());
    }

    private static GameResultDto playWithSeed(long seed) {
        PrintStream silentConsole = new PrintStream(new ByteArrayOutputStream());
        return new GameConfiguration(seed, silentConsole).createGame().play();
    }
}
