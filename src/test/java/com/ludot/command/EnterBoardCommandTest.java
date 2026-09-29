package com.ludot.command;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.MoveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnterBoardCommandTest {

    private CommandTestFixture fixture;
    private Piece red1;

    @BeforeEach
    void setUp() {
        fixture = new CommandTestFixture();
        red1 = new Piece(Colour.RED, 1);
    }

    @Test
    void pieceIsPlacedOnItsStartCell() {
        enter(true);
        assertEquals(Position.onTrack(26), red1.position());
    }

    @Test
    @DisplayName("Rule T-1: heads means clockwise")
    void headsMeansClockwise() {
        enter(true);
        assertEquals(Direction.CLOCKWISE, red1.direction());
    }

    @Test
    @DisplayName("Rule T-1: tails means counter-clockwise")
    void tailsMeansCounterClockwise() {
        enter(false);
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    @Test
    void entryIsReported() {
        enter(true);
        assertTrue(fixture.observer.hasEvent("entered R1"));
    }

    @Test
    @DisplayName("Rule 6 + T-2: entering onto an opponent captures it and earns a bonus roll")
    void enteringOntoAnOpponentCapturesIt() {
        Piece green1 = new Piece(Colour.GREEN, 1);
        fixture.placeOnTrack(green1, 26);
        TurnOutcome outcome = enter(true);
        assertTrue(green1.isInBase());
        assertTrue(outcome.grantsBonusRoll());
    }

    private TurnOutcome enter(boolean coinHeads) {
        return fixture.factoryWith(coinHeads)
                .create(fixture.option(List.of(red1), 6, MoveType.ENTER_BOARD))
                .execute();
    }
}