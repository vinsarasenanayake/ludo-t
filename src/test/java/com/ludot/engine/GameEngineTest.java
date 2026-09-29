package com.ludot.engine;

import com.ludot.domain.Colour;
import com.ludot.port.CellPicker;
import com.ludot.port.Coin;
import com.ludot.port.Dice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineTest {

    private static final List<Colour> TURN_ORDER = List.of(Colour.RED, Colour.GREEN, Colour.YELLOW, Colour.BLUE);

    @Test
    void everyPlayerIsIntroduced() {
        EngineTestFixture fixture = seededFixture(1);
        fixture.engine().introducePlayers();
        assertTrue(fixture.observer.hasEvent("introduced BLUE"));
    }

    @Test
    @DisplayName("Rule 11: a full seeded game finishes with all four players ranked")
    void fullGameRanksAllFourPlayers() {
        GameOutcome outcome = seededFixture(1).engine().run(TURN_ORDER);
        assertEquals(4, outcome.finishingOrder().size());
        assertFalse(outcome.stalled());
    }

    @Test
    @DisplayName("Rule 11: the first player to bring all pieces home wins")
    void winnerIsReported() {
        EngineTestFixture fixture = seededFixture(1);
        GameOutcome outcome = fixture.engine().run(TURN_ORDER);
        assertTrue(fixture.observer.hasEvent("finished " + outcome.finishingOrder().get(0) + " 1"));
    }

    @Test
    void mysteryCellAppearsDuringAGame() {
        EngineTestFixture fixture = seededFixture(1);
        fixture.engine().run(TURN_ORDER);
        assertTrue(fixture.observer.events().stream().anyMatch(event -> event.startsWith("mystery spawned")));
    }

    private EngineTestFixture seededFixture(long seed) {
        Random random = new Random(seed);
        Dice dice = () -> random.nextInt(6) + 1;
        Coin coin = random::nextBoolean;
        CellPicker cellPicker = cells -> cells.get(random.nextInt(cells.size()));
        return new EngineTestFixture(dice, coin, cellPicker);
    }
}
