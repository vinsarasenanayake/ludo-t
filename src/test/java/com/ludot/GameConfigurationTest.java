package com.ludot;

import com.ludot.board.Colour;
import com.ludot.game.GameResultDto;
import com.ludot.game.LudoGameFacade;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameConfigurationTest {

    // Design: the composition root wires a whole game that the Facade plays, and the same seed repeats it
    @Test
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

    // R11 + Brief 3.1: the default seed 42 always plays the same game, which Green wins after 167 rounds
    @Test
    void defaultSeedGameIsWonByGreen() {
        PrintStream silentConsole = new PrintStream(new ByteArrayOutputStream());
        GameResultDto result = new GameConfiguration(42L, silentConsole).createGame().play();
        assertEquals(List.of(Colour.GREEN, Colour.YELLOW, Colour.RED, Colour.BLUE), result.finishingOrder());
        assertEquals(167, result.rounds());
        assertFalse(result.stalled());
    }
}
