package com.ludot.command;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.MoveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveBlockCommandTest {

    private CommandTestFixture fixture;
    private Piece red1;
    private Piece red2;

    @BeforeEach
    void setUp() {
        fixture = new CommandTestFixture();
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
        fixture.placeOnTrack(red1, 0);
        fixture.placeOnTrack(red2, 0);
    }

    @Test
    @DisplayName("Rule T-4: every piece of the block moves together")
    void everyPieceOfTheBlockMoves() {
        moveBlock(6);
        assertEquals(Position.onTrack(3), red1.position());
        assertEquals(Position.onTrack(3), red2.position());
        assertTrue(fixture.board.isBlockAt(3));
    }

    @Test
    @DisplayName("Rule T-8: capturing a block gives every capturing piece a capture")
    void capturingABlockCountsForEveryPiece() {
        Piece green1 = new Piece(Colour.GREEN, 1);
        Piece green2 = new Piece(Colour.GREEN, 2);
        fixture.placeOnTrack(green1, 3);
        fixture.placeOnTrack(green2, 3);
        TurnOutcome outcome = moveBlock(6);
        assertTrue(green1.isInBase());
        assertTrue(green2.isInBase());
        assertEquals(1, red1.captureCount());
        assertEquals(1, red2.captureCount());
        assertTrue(outcome.grantsBonusRoll());
    }

    private TurnOutcome moveBlock(int roll) {
        return fixture.factoryWith(true)
                .create(fixture.option(List.of(red1, red2), roll, MoveType.MOVE_BLOCK))
                .execute();
    }
}