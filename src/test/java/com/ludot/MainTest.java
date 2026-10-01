package com.ludot;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // Design: main reports an invalid seed as a message instead of crashing
    @Test
    void invalidSeedIsReportedWithoutCrashing() {
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errors, true));
        try {
            Main.main(new String[]{"abc"});
        } finally {
            System.setErr(originalErr);
        }
        assertTrue(errors.toString().contains("The seed must be a whole number but was: abc"));
    }

    // Design: a valid seed, including a negative one, is read as a number
    @Test
    void validSeedIsParsed() {
        assertEquals(7L, Main.parseSeed("7"));
        assertEquals(-3L, Main.parseSeed("-3"));
    }

    // Brief 3.1: running the program prints the whole default game to the terminal, ending with the results
    @Test
    void mainPrintsTheWholeGameToTheTerminal() {
        ByteArrayOutputStream terminal = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(terminal, true));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }
        String output = terminal.toString();
        assertTrue(output.startsWith("The red player has four (04) pieces named R1, R2, R3, and R4."));
        assertTrue(output.contains("Green player has the highest roll and will begin the game."));
        assertTrue(output.contains("Green player wins!!!"));
        assertTrue(output.contains("Final results after 167 rounds"));
        assertTrue(output.strip().endsWith("4th place: Blue"));
    }
}
