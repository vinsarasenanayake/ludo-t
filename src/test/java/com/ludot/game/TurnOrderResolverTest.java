package com.ludot.game;

import com.ludot.board.Colour;
import com.ludot.output.RecordingObserver;
import com.ludot.random.Dice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.random.RandomMocks.diceRolling;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TurnOrderResolverTest {

    private static final List<Colour> PLAYERS = List.of(Colour.RED, Colour.GREEN, Colour.YELLOW, Colour.BLUE);

    private final RecordingObserver observer = new RecordingObserver();

    // Brief 3.1: the highest opening roll starts, then play goes clockwise
    @Test
    void highestRollerStartsAndOrderIsClockwise() {
        TurnOrderResolver resolver = new TurnOrderResolver(diceRolling(2, 3, 6, 1), observer);
        assertEquals(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN), resolver.resolve(PLAYERS));
    }

    // Brief 3.1: the round wraps round clockwise from whoever starts
    @Test
    void turnOrderWrapsRoundFromTheStarter() {
        TurnOrderResolver resolver = new TurnOrderResolver(diceRolling(1, 2, 3, 6), observer);
        assertEquals(List.of(Colour.BLUE, Colour.RED, Colour.GREEN, Colour.YELLOW), resolver.resolve(PLAYERS));
    }

    // Brief 3.1: every opening roll is reported
    @Test
    void everyOpeningRollIsReported() {
        new TurnOrderResolver(diceRolling(2, 3, 6, 1), observer).resolve(PLAYERS);
        assertTrue(observer.hasEvent("opening roll RED 2"));
        assertTrue(observer.hasEvent("opening roll BLUE 1"));
    }

    // A3: a two-way tie for the highest roll is broken by the tied players rolling again
    @Test
    void tiedPlayersRollAgain() {
        Dice dice = diceRolling(6, 6, 1, 2, 3, 5);
        TurnOrderResolver resolver = new TurnOrderResolver(dice, observer);
        assertEquals(Colour.GREEN, resolver.resolve(PLAYERS).get(0));
        verify(dice, times(6)).roll();
    }

    // A3: in a three-way tie only the three tied players roll again
    @Test
    void threeWayTieRollsAgainAmongTheTiedOnly() {
        Dice dice = diceRolling(6, 6, 6, 2, 3, 5, 1);
        TurnOrderResolver resolver = new TurnOrderResolver(dice, observer);
        assertEquals(List.of(Colour.GREEN, Colour.YELLOW, Colour.BLUE, Colour.RED), resolver.resolve(PLAYERS));
        verify(dice, times(7)).roll();
    }
}
