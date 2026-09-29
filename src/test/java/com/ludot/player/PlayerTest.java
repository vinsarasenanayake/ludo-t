package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.MoveOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    private Player red;

    @BeforeEach
    void setUp() {
        red = new Player(Colour.RED, context -> context.options().get(0));
    }

    @Test
    @DisplayName("Each player has four pieces named 1 to 4")
    void playerHasFourPiecesNamedOneToFour() {
        List<String> names = red.pieces().stream().map(Piece::name).toList();
        assertEquals(List.of("R1", "R2", "R3", "R4"), names);
    }

    @Test
    void allPiecesStartInBase() {
        assertEquals(4, red.piecesInBase());
        assertEquals(0, red.piecesOnBoard());
    }

    @Test
    void pieceOnTheTrackCountsAsOnTheBoard() {
        red.pieces().get(0).enterBoard(Direction.CLOCKWISE);
        assertEquals(1, red.piecesOnBoard());
        assertTrue(red.hasPieceOnTrack());
    }

    @Test
    void playerHasNotFinishedWhilePiecesAreOut() {
        red.pieces().get(0).moveTo(Position.home());
        assertFalse(red.hasFinished());
    }

    @Test
    @DisplayName("Rule 11: a player finishes when all four pieces are home")
    void playerFinishesWhenAllPiecesAreHome() {
        red.pieces().forEach(piece -> piece.moveTo(Position.home()));
        assertTrue(red.hasFinished());
    }

    @Test
    @DisplayName("Strategy: the player delegates the decision to its strategy")
    void chooseMoveDelegatesToTheStrategy() {
        MoveOption option = move(red.pieces().get(0));
        assertSame(option, red.chooseMove(context(option)));
    }
}
