package com.ludot.command;

import com.ludot.domain.Colour;
import com.ludot.testsupport.RecordingObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullMoveCommandTest {

    @Test
    @DisplayName("Null Object: no move means nothing happens and no bonus roll")
    void returnsNothing() {
        NullMoveCommand command = new NullMoveCommand(Colour.RED, new RecordingObserver());
        assertSame(TurnOutcome.NOTHING, command.execute());
    }

    @Test
    @DisplayName("Rule 7: the ignored throw is reported")
    void reportsTheIgnoredThrow() {
        RecordingObserver observer = new RecordingObserver();
        new NullMoveCommand(Colour.RED, observer).execute();
        assertTrue(observer.hasEvent("no move RED"));
    }
}