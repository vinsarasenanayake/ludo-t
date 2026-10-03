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

    @Test
    void highestRollerStartsAndOrderIsClockwise() {
        TurnOrderResolver resolver = new TurnOrderResolver(diceRolling(2, 3, 6, 1), observer);
        assertEquals(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN), resolver.resolve(PLAYERS));
    }

    @Test
    void everyOpeningRollIsReported() {
        new TurnOrderResolver(diceRolling(2, 3, 6, 1), observer).resolve(PLAYERS);
        assertTrue(observer.hasEvent("opening roll RED 2"));
        assertTrue(observer.hasEvent("opening roll BLUE 1"));
    }

    @Test
    void threeWayTieRollsAgainAmongTheTiedOnly() {
        Dice dice = diceRolling(6, 6, 6, 2, 3, 5, 1);
        TurnOrderResolver resolver = new TurnOrderResolver(dice, observer);
        assertEquals(List.of(Colour.GREEN, Colour.YELLOW, Colour.BLUE, Colour.RED), resolver.resolve(PLAYERS));
        verify(dice, times(7)).roll();
    }
}