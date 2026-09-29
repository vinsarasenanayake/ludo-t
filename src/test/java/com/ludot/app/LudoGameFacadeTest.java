package com.ludot.app;

import com.ludot.dto.GameResultDto;
import com.ludot.testsupport.InMemoryGameResultGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LudoGameFacadeTest {

    @Test
    @DisplayName("Facade: one call plays a whole game and saves the result")
    void playingAGameSavesItsResult() {
        InMemoryGameResultGateway results = new InMemoryGameResultGateway();
        GameResultDto result = new GameConfiguration(42L, silentConsole(), results).createGame().play();
        assertEquals(1, results.findAll().size());
        assertEquals(4, result.finishingOrder().size());
    }

    @Test
    @DisplayName("The same seed always produces the same game")
    void sameSeedGivesSameResult() {
        GameResultDto first = new GameConfiguration(7L, silentConsole(), new InMemoryGameResultGateway()).createGame().play();
        GameResultDto second = new GameConfiguration(7L, silentConsole(), new InMemoryGameResultGateway()).createGame().play();
        assertEquals(first, second);
    }

    @Test
    void theGameOutputStartsWithThePlayerIntroductions() {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new GameConfiguration(42L, new PrintStream(printed, true), new InMemoryGameResultGateway()).createGame().play();
        assertTrue(printed.toString().startsWith("The red player has four (04) pieces named R1, R2, R3, and R4."));
    }

    private PrintStream silentConsole() {
        return new PrintStream(new ByteArrayOutputStream());
    }
}