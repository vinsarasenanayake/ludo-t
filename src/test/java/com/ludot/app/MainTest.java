package com.ludot.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    @DisplayName("Design: an invalid seed raises a custom exception that keeps its cause, and main reports it cleanly")
    void invalidSeedIsReportedWithoutCrashing() {
        Main.InvalidSeedException error = assertThrows(Main.InvalidSeedException.class, () -> Main.parseSeed("abc"));
        assertInstanceOf(NumberFormatException.class, error.getCause());

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
}
