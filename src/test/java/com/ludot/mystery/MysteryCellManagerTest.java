package com.ludot.mystery;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.output.RecordingObserver;
import com.ludot.random.CellPicker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MysteryCellManagerTest {

    private final CellPicker cellPicker = mock(CellPicker.class);
    private Board board;
    private RecordingObserver observer;

    @BeforeEach
    void setUp() {
        board = new Board();
        observer = new RecordingObserver();
    }

    // T-10: no mystery cell while no piece is on the track
    @Test
    void noMysteryCellWhileNoPieceIsOnTheTrack() {
        MysteryCellManager manager = managerPicking(10);
        endRounds(manager, false, 5);
        assertFalse(manager.current().isActive());
    }

    // T-10: spawns only after two further rounds with pieces on the track
    @Test
    void spawnsAfterTwoRounds() {
        MysteryCellManager manager = managerPicking(10);
        endRounds(manager, true, 2);
        assertFalse(manager.current().isActive());
        manager.endRound(true);
        assertEquals(10, manager.current().location());
        assertTrue(observer.hasEvent("mystery spawned 10"));
    }

    // T-10: only spawns on a cell with no pieces
    @Test
    void spawnsOnlyOnAnEmptyCell() {
        board.enter(new Piece(Colour.RED, 1), Direction.CLOCKWISE);
        MysteryCellManager manager = managerPicking(0);
        endRounds(manager, true, 3);
        verify(cellPicker).pick(argThat(cells -> !cells.contains(26)));
        assertEquals(0, manager.current().location());
    }

    // T-10: stays for four rounds, then moves to a different cell
    @Test
    void staysForFourRoundsThenRelocates() {
        MysteryCellManager manager = managerPicking(10, 0);
        endRounds(manager, true, 6);
        assertEquals(10, manager.current().location());
        manager.endRound(true);
        verify(cellPicker).pick(argThat(cells -> !cells.contains(10)));
        assertEquals(0, manager.current().location());
    }

    // T-10: every new mystery cell is announced, including the move to a new cell
    @Test
    void everySpawnIsReported() {
        MysteryCellManager manager = managerPicking(10, 0);
        endRounds(manager, true, 7);
        assertTrue(observer.hasEvent("mystery spawned 10"));
        assertTrue(observer.hasEvent("mystery spawned 0"));
    }

    private MysteryCellManager managerPicking(Integer firstCell, Integer... laterCells) {
        when(cellPicker.pick(anyList())).thenReturn(firstCell, laterCells);
        return new MysteryCellManager(board, cellPicker, observer);
    }

    private void endRounds(MysteryCellManager manager, boolean anyPieceOnTrack, int rounds) {
        for (int round = 0; round < rounds; round++) {
            manager.endRound(anyPieceOnTrack);
        }
    }
}
