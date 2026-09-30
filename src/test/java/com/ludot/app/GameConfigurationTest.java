package com.ludot.app;

import com.ludot.game.GameResultDto;
import com.ludot.game.LudoGameFacade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameConfigurationTest {

    @Test
    @DisplayName("Design: the composition root wires a whole game that the Facade plays, and the same seed repeats it")
    void configuredGameIsPlayedByTheFacadeAndRepeatable() {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        LudoGameFacade game = new GameConfiguration(42L, new PrintStream(printed, true)).createGame();
        GameResultDto first = game.play();
        PrintStream silentConsole = new PrintStream(new ByteArrayOutputStream());
        GameResultDto second = new GameConfiguration(42L, silentConsole).createGame().play();
        assertEquals(4, first.finishingOrder().size());
        assertEquals(first, second);
        assertTrue(printed.toString().startsWith("The red player has four (04) pieces named R1, R2, R3, and R4."));
    }
}
