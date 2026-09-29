package com.ludot.app;

import com.ludot.dto.GameResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LudoGameFacadeTest {

    @Test
    @DisplayName("Facade: one call plays a whole game and returns its result")
    void playingAGameReturnsItsResult() {
        GameResultDto result = new LudoGameFacade(42L, silentConsole()).play();
        assertEquals(4, result.finishingOrder().size());
        assertTrue(result.rounds() > 0);
    }

    @Test
    @DisplayName("The same seed always produces the same game")
    void sameSeedGivesSameResult() {
        GameResultDto first = new LudoGameFacade(7L, silentConsole()).play();
        GameResultDto second = new LudoGameFacade(7L, silentConsole()).play();
        assertEquals(first, second);
    }

    @Test
    void theGameOutputStartsWithThePlayerIntroductions() {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        new LudoGameFacade(42L, new PrintStream(printed, true)).play();
        assertTrue(printed.toString().startsWith("The red player has four (04) pieces named R1, R2, R3, and R4."));
    }

    @Test
    @DisplayName("A seed that is not a number is rejected, keeping the original error as the cause")
    void invalidSeedIsRejected() {
        Main.InvalidSeedException error = assertThrows(Main.InvalidSeedException.class,
                () -> Main.main(new String[]{"abc"}));
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    private PrintStream silentConsole() {
        return new PrintStream(new ByteArrayOutputStream());
    }
}
