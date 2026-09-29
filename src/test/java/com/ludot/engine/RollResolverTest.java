package com.ludot.engine;

import com.ludot.command.TurnOutcome;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.player.Player;
import com.ludot.testsupport.FixedCellPicker;
import com.ludot.testsupport.FixedCoin;
import com.ludot.testsupport.ScriptedDice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RollResolverTest {

    private EngineTestFixture fixture;
    private Player red;

    @BeforeEach
    void setUp() {
        fixture = new EngineTestFixture(new ScriptedDice(), new FixedCoin(true), new FixedCellPicker(0));
        red = fixture.player(Colour.RED);
    }

    @Test
    @DisplayName("Rule 7: with no legal move the throw is ignored")
    void noLegalMoveIgnoresTheThrow() {
        TurnOutcome outcome = fixture.rollResolver.resolve(red, 4);
        assertSame(TurnOutcome.NOTHING, outcome);
        assertTrue(fixture.observer.hasEvent("no move RED"));
    }

    @Test
    void chosenMoveIsExecuted() {
        fixture.rollResolver.resolve(red, 6);
        assertEquals(Position.onTrack(26), red.pieces().get(0).position());
    }

    @Test
    @DisplayName("Rule T-6: breaking a blockade moves every piece but one")
    void breakingABlockadeMovesAllButOnePiece() {
        Piece red1 = red.pieces().get(0);
        Piece red2 = red.pieces().get(1);
        fixture.board.enter(red1, Direction.CLOCKWISE);
        fixture.board.enter(red2, Direction.CLOCKWISE);
        fixture.rollResolver.breakBlockades(red);
        assertEquals(Position.onTrack(26), red1.position());
        assertEquals(Position.onTrack(32), red2.position());
    }
}
