package com.ludot.engine;

import com.ludot.domain.Colour;
import com.ludot.player.Player;
import com.ludot.testsupport.FixedCellPicker;
import com.ludot.testsupport.FixedCoin;
import com.ludot.testsupport.ScriptedDice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnProcessorTest {

    @Test
    void nonSixGivesOneRollOnly() {
        EngineTestFixture fixture = fixtureRolling(3);
        fixture.turnProcessor.playTurn(fixture.player(Colour.RED));
        assertEquals(1, countRolls(fixture));
    }

    @Test
    @DisplayName("Rule 4: a six gives another roll")
    void sixGivesAnotherRoll() {
        EngineTestFixture fixture = fixtureRolling(6, 2);
        fixture.turnProcessor.playTurn(fixture.player(Colour.RED));
        assertEquals(2, countRolls(fixture));
    }

    @Test
    @DisplayName("Rule 4: the third six in a row is ignored and the turn ends")
    void thirdSixIsIgnored() {
        EngineTestFixture fixture = fixtureRolling(6, 6, 6);
        fixture.turnProcessor.playTurn(fixture.player(Colour.RED));
        assertTrue(fixture.observer.hasEvent("roll ignored RED"));
        assertEquals(3, countRolls(fixture));
    }

    @Test
    void enteringThePieceReportsTheNewStatus() {
        EngineTestFixture fixture = fixtureRolling(6, 1);
        Player red = fixture.player(Colour.RED);
        fixture.turnProcessor.playTurn(red);
        assertTrue(fixture.observer.hasEvent("status RED 1/3"));
    }

    private EngineTestFixture fixtureRolling(int... rolls) {
        return new EngineTestFixture(new ScriptedDice(rolls), new FixedCoin(true), new FixedCellPicker(0));
    }

    private int countRolls(EngineTestFixture fixture) {
        return (int) fixture.observer.events().stream().filter(event -> event.startsWith("rolled")).count();
    }
}
