package com.ludot;

import com.ludot.board.Colour;
import com.ludot.game.GameResultDto;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameConfigurationTest {

    @Test
    void sameSeedPlaysTheSameGame() {
        assertEquals(playDefaultSeedGame(), playDefaultSeedGame());
    }

    @Test
    void defaultSeedGameMatchesItsRecordedResult() {
        GameResultDto result = playDefaultSeedGame();
        assertEquals(List.of(Colour.GREEN, Colour.YELLOW, Colour.BLUE, Colour.RED), result.finishingOrder());
        assertEquals(220, result.rounds());
    }

    private static GameResultDto playDefaultSeedGame() {
        PrintStream silentConsole = new PrintStream(new ByteArrayOutputStream());
        return new GameConfiguration(42L, silentConsole).createGame().play();
    }
}