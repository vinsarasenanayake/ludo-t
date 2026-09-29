package com.ludot.app;

import com.ludot.dto.GameResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LudoGameFacadeTest {

    @Test
    @DisplayName("Facade: one call plays a whole game and returns its result")
    void playingAGameReturnsItsResult() {
        GameResultDto result = new GameConfiguration(42L, silentConsole()).createGame().play();
        assertEquals(42L, result.seed());
        assertEquals(4, result.finishingOrder().size());
        assertTrue(result.rounds() > 0);
    }

    @Test
    @DisplayName("The same seed always produces the same game")
    void sameSeedGivesSameResult() {
        GameResultDto first = new GameConfiguration(7L, silentConsole()).createGame().play();
        GameResultDto second = new GameConfiguration(7L, silentConsole()).createGame().play();
        assertEquals(first, second);
    }

    @Test
    void theGameOutputStartsWithThePlayerIntroductions() {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new GameConfiguration(42L, new PrintStream(printed, true)).createGame().play();
        assertTrue(printed.toString().startsWith("The red player has four (04) pieces named R1, R2, R3, and R4."));
    }

    private PrintStream silentConsole() {
        return new PrintStream(new ByteArrayOutputStream());
    }
}
