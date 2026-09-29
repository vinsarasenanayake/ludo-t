package com.ludot.output;

import com.ludot.board.Colour;
import com.ludot.mystery.MysteryCell;
import com.ludot.player.PlayerStatusDto;
import com.ludot.random.SeededRandomness;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleReporterTest {

    private static final String NEW_LINE = System.lineSeparator();

    private ByteArrayOutputStream printed;
    private ConsoleReporter reporter;
    @BeforeEach
    void setUp() {
        printed = new ByteArrayOutputStream();
        reporter = new ConsoleReporter(new PrintStream(printed, true));
    }

    @Test
    @DisplayName("Brief 3.1: messages before the game begins")
    void playerIntroductionAndTurnOrder() {
        reporter.onPlayerIntroduced(Colour.RED);
        reporter.onTurnOrderDecided(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN));
        assertPrinted("The red player has four (04) pieces named R1, R2, R3, and R4." + NEW_LINE
                + "Yellow player has the highest roll and will begin the game." + NEW_LINE
                + "The order of a single round is Yellow, Blue, Red, and Green.");
    }

    @Test
    @DisplayName("Assumption A17: the status line keeps the brief's exact wording")
    void playerStatus() {
        reporter.onPlayerStatus(new PlayerStatusDto(Colour.RED, 1, 3, List.of()));
        assertPrinted("Red player now has 1/4 on pieces on the board and 3/4 pieces on the base.");
    }

    @Test
    @DisplayName("Brief 3.1: after each round, piece locations and the mystery cell are shown")
    void roundSummaryListsPieceLocationsAndMysteryCell() {
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 1, 3, List.of(
                new PlayerStatusDto.PieceLocation("R1", "30"), new PlayerStatusDto.PieceLocation("R2", "Base")));
        reporter.onRoundEnded(List.of(status), new MysteryCell.Active(17, 3));
        String summary = printed.toString();
        assertTrue(summary.contains("Location of pieces Red"));
        assertTrue(summary.contains("Piece R1 -> 30"));
        assertTrue(summary.contains("The mystery cell is at 17 and will be at that location for the next 3 rounds."));
    }

    @Test
    @DisplayName("Brief 3.1: upon winning, and the places after it")
    void winnerAndLaterPlaces() {
        reporter.onPlayerFinished(Colour.RED, 1);
        reporter.onPlayerFinished(Colour.BLUE, 2);
        assertPrinted("Red player wins!!!" + NEW_LINE + "Blue player finishes in place 2.");
    }

    @Test
    @DisplayName("Seeded randomness: dice stay within 1-6 and the same seed repeats, so demos are repeatable")
    void seededDiceAreValidAndRepeatable() {
        SeededRandomness first = new SeededRandomness(42);
        SeededRandomness second = new SeededRandomness(42);
        for (int roll = 0; roll < 100; roll++) {
            int value = first.roll();
            assertTrue(value >= 1 && value <= 6);
            assertEquals(value, second.roll());
        }
    }

    private void assertPrinted(String expected) {
        assertEquals(expected + NEW_LINE, printed.toString());
    }
}
