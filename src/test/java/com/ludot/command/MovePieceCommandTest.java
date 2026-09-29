package com.ludot.command;

import com.ludot.domain.ActiveMysteryCell;
import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.MoveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovePieceCommandTest {

    private static final int BETA_FACE = 2;

    private CommandTestFixture fixture;
    private Piece red1;
    private Piece green1;
    private Piece green2;

    @BeforeEach
    void setUp() {
        fixture = new CommandTestFixture();
        red1 = new Piece(Colour.RED, 1);
        green1 = new Piece(Colour.GREEN, 1);
        green2 = new Piece(Colour.GREEN, 2);
    }

    @Test
    void pieceMovesToTheDestination() {
        fixture.placeOnTrack(red1, 26);
        move(4);
        assertEquals(Position.onTrack(30), red1.position());
        assertTrue(fixture.observer.hasEvent("moved R1 to 30"));
    }

    @Test
    void plainMoveGivesNoBonusRoll() {
        fixture.placeOnTrack(red1, 26);
        assertFalse(move(4).grantsBonusRoll());
    }

    @Test
    @DisplayName("Rule 6 + T-2: a capture sends the victim to base and earns a bonus roll")
    void captureSendsVictimHomeAndGivesBonus() {
        fixture.placeOnTrack(red1, 26);
        fixture.placeOnTrack(green1, 30);
        TurnOutcome outcome = move(4);
        assertTrue(green1.isInBase());
        assertEquals(1, red1.captureCount());
        assertTrue(outcome.grantsBonusRoll());
        assertTrue(fixture.observer.hasEvent("capture R1 G1"));
    }

    @Test
    @DisplayName("Rule T-3: a blocked piece is reported and stops before the block")
    void blockedMoveIsReported() {
        fixture.placeOnTrack(red1, 0);
        fixture.placeOnTrack(green1, 4);
        fixture.placeOnTrack(green2, 4);
        move(6);
        assertEquals(Position.onTrack(3), red1.position());
        assertTrue(fixture.observer.hasEvent("blocked R1 by G1"));
    }

    @Test
    void passingTheApproachIsRecorded() {
        fixture.placeOnTrack(red1, 22);
        move(3);
        assertEquals(1, red1.approachPasses());
    }

    @Test
    @DisplayName("Rule T-11: landing on the mystery cell teleports the piece")
    void landingOnMysteryCellTeleports() {
        fixture.placeOnTrack(red1, 26);
        fixture.factoryWith(true, BETA_FACE)
                .create(fixture.option(List.of(red1), 4, MoveType.MOVE_PIECE, new ActiveMysteryCell(30, 4)))
                .execute();
        assertEquals(Position.onTrack(25), red1.position());
    }

    private TurnOutcome move(int roll) {
        return fixture.factoryWith(true)
                .create(fixture.option(List.of(red1), roll, MoveType.MOVE_PIECE))
                .execute();
    }
}