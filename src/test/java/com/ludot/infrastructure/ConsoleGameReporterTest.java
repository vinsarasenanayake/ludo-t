package com.ludot.infrastructure;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleGameReporterTest {

    private ByteArrayOutputStream printed;
    private ConsoleGameReporter reporter;

    @BeforeEach
    void setUp() {
        printed = new ByteArrayOutputStream();
        reporter = new ConsoleGameReporter(new MessageFormatter(), new PrintStream(printed, true));
    }

    @Test
    void printsTheDiceRollAsOneLine() {
        reporter.onDiceRolled(Colour.RED, 4);
        assertEquals("Red player rolled 4." + System.lineSeparator(), printed.toString());
    }

    @Test
    void printsTheWinner() {
        reporter.onPlayerFinished(Colour.BLUE, 1);
        assertEquals("Blue player wins!!!" + System.lineSeparator(), printed.toString());
    }

    @Test
    void printsTheStartingPointMessage() {
        reporter.onPieceEntered(new Piece(Colour.GREEN, 2));
        assertEquals("Green player moves piece G2 to the starting point." + System.lineSeparator(), printed.toString());
    }
}
