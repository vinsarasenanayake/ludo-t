package com.ludot;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    // Design: an invalid seed raises a custom exception that keeps the original error as its cause
    @Test
    void invalidSeedRaisesACustomException() {
        Main.InvalidSeedException error = assertThrows(Main.InvalidSeedException.class, () -> Main.parseSeed("abc"));
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    // Design: an invalid seed is reported as a message instead of crashing the program
    @Test
    void invalidSeedIsReportedOnTheErrorStream() {
        PrintStream originalError = System.err;
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(errors, true));
            Main.main(new String[] {"abc"});
        } finally {
            System.setErr(originalError);
        }
        assertTrue(errors.toString().contains("The seed must be a whole number but was: abc"));
    }

    // Brief 2 + 3.1: running the program with no arguments simulates a whole game and prints it
    @Test
    void runningWithoutArgumentsPrintsAWholeGame() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream terminal = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(terminal, true));
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }
        String output = terminal.toString();
        assertTrue(output.startsWith("The red player has four (04) pieces named R1, R2, R3, and R4."));
        assertTrue(output.contains("player has the highest roll and will begin the game."));
        assertTrue(output.contains("player wins!!!"));
        assertTrue(output.contains("4th place: "));
    }
}
