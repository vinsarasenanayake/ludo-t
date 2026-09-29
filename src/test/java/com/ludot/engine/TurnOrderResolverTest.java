package com.ludot.engine;

import com.ludot.domain.Colour;
import com.ludot.testsupport.RecordingObserver;
import com.ludot.testsupport.ScriptedDice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnOrderResolverTest {

    private static final List<Colour> ALL = List.of(Colour.RED, Colour.GREEN, Colour.YELLOW, Colour.BLUE);

    private final RecordingObserver observer = new RecordingObserver();

    @Test
    @DisplayName("The highest opening roll starts, then play goes clockwise")
    void highestRollerStartsAndOrderIsClockwise() {
        TurnOrderResolver resolver = new TurnOrderResolver(new ScriptedDice(2, 3, 6, 1), observer);
        assertEquals(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN), resolver.resolve(ALL));
    }

    @Test
    @DisplayName("Assumption A14: only tied players roll again")
    void tiedPlayersRollAgain() {
        TurnOrderResolver resolver = new TurnOrderResolver(new ScriptedDice(6, 6, 1, 2, 3, 5), observer);
        assertEquals(Colour.GREEN, resolver.resolve(ALL).get(0));
    }

    @Test
    void everyOpeningRollIsReported() {
        new TurnOrderResolver(new ScriptedDice(2, 3, 6, 1), observer).resolve(ALL);
        assertTrue(observer.hasEvent("opening roll YELLOW 6"));
    }
}