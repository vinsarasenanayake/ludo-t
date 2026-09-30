package com.ludot.mystery;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.testsupport.TestDoubles.RecordingObserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ludot.testsupport.TestDoubles.preferring;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MysteryCellManagerTest {

    private Board board;
    private RecordingObserver observer;

    @BeforeEach
    void setUp() {
        board = new Board();
        observer = new RecordingObserver();
    }

    @Test
    @DisplayName("T-10: no mystery cell while no piece is on the track")
    void noMysteryCellWhileNoPieceIsOnTheTrack() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, false, 5);
        assertFalse(manager.current().isActive());
    }

    @Test
    @DisplayName("T-10: spawns only after two further rounds with pieces on the track")
    void spawnsAfterTwoRounds() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, true, 2);
        assertFalse(manager.current().isActive());
        manager.endRound(true);
        assertEquals(10, manager.current().location());
        assertTrue(observer.hasEvent("mystery spawned 10"));
    }

    @Test
    @DisplayName("T-10: only spawns on a cell with no pieces")
    void spawnsOnlyOnAnEmptyCell() {
        board.enter(new Piece(Colour.RED, 1), Direction.CLOCKWISE);
        MysteryCellManager manager = managerPreferring(26);
        endRounds(manager, true, 3);
        assertNotEquals(26, manager.current().location());
    }

    @Test
    @DisplayName("T-10: stays for four rounds, then moves to a different cell")
    void staysForFourRoundsThenRelocates() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, true, 6);
        assertEquals(10, manager.current().location());
        manager.endRound(true);
        assertTrue(manager.current().isActive());
        assertNotEquals(10, manager.current().location());
    }

    private MysteryCellManager managerPreferring(int cell) {
        return new MysteryCellManager(board, preferring(cell), observer);
    }

    private void endRounds(MysteryCellManager manager, boolean anyPieceOnTrack, int rounds) {
        for (int round = 0; round < rounds; round++) {
            manager.endRound(anyPieceOnTrack);
        }
    }
}
