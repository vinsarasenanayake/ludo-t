package com.ludot.player;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    private final Board board = new Board();
    private final Player red = new PlayerFactory(new TrackNavigator()).createPlayer(Colour.RED);
    private final List<Piece> redPieces = red.pieces();

    @Test
    void playerStartsWithFourNamedPiecesInBase() {
        assertEquals(List.of("R1", "R2", "R3", "R4"), redPieces.stream().map(Piece::name).toList());
        assertEquals(4, red.piecesInBase());
    }

    @Test
    void playerHasNotFinishedWithAPieceStillOut() {
        redPieces.subList(0, 3).forEach(piece -> board.move(piece, Position.home()));
        assertFalse(red.hasFinished());
    }

    @Test
    void playerFinishesWhenAllFourPiecesAreHome() {
        redPieces.forEach(piece -> board.move(piece, Position.home()));
        assertTrue(red.hasFinished());
    }

    @Test
    void statusDescribesEveryPiece() {
        board.enter(redPieces.getFirst(), Direction.CLOCKWISE);
        PlayerStatusDto status = red.status();
        assertEquals(1, status.piecesOnBoard());
        assertEquals(3, status.piecesInBase());
        assertEquals(new PlayerStatusDto.PieceLocation("R1", "26"), status.pieces().getFirst());
    }

    @Test
    void homeStraightPieceCountsAsOnTheBoard() {
        board.move(redPieces.getFirst(), Position.inHomeStraight(2));
        assertEquals(1, red.piecesOnBoard());
    }
}