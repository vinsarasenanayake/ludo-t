package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
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
    void noMysteryCellWhileNoPieceIsOnTheTrack() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, false, 5);
        assertFalse(manager.current().isActive());
    }

    @Test
    @DisplayName("Rule T-10: not yet one round after pieces reach the track")
    void noSpawnAfterOnlyOneFurtherRound() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, true, 2);
        assertFalse(manager.current().isActive());
    }

    @Test
    @DisplayName("Rule T-10: spawns once two rounds have passed with pieces on the track")
    void spawnsAfterTwoRounds() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, true, 2);
        manager.endRound(true);
        assertEquals(10, manager.current().location());
        assertTrue(observer.hasEvent("mystery spawned 10"));
    }

    @Test
    @DisplayName("Rule T-10: only spawns on a cell with no pieces")
    void spawnsOnlyOnAnEmptyCell() {
        board.enter(new Piece(Colour.RED, 1), Direction.CLOCKWISE);
        MysteryCellManager manager = managerPreferring(26);
        endRounds(manager, true, 3);
        assertNotEquals(26, manager.current().location());
    }

    @Test
    @DisplayName("Rule T-10: stays in the same cell for four rounds")
    void staysForFourRounds() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, true, 3);
        endRounds(manager, true, 3);
        assertEquals(10, manager.current().location());
        assertEquals(1, manager.current().roundsRemaining());
    }

    @Test
    @DisplayName("Rule T-10: moves after four rounds and never to the same cell")
    void relocatesToADifferentCell() {
        MysteryCellManager manager = managerPreferring(10);
        endRounds(manager, true, 3);
        endRounds(manager, true, 4);
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
