package com.ludot.engine;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.dto.PieceLocationDto;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusSnapshotFactoryTest {

    private final StatusSnapshotFactory snapshots = new StatusSnapshotFactory();

    @Test
    void countsPiecesOnTheBoardAndInBase() {
        Player red = new Player(Colour.RED, context -> context.options().get(0));
        red.pieces().get(0).enterBoard(Direction.CLOCKWISE);
        PlayerStatusDto status = snapshots.snapshotOf(red);
        assertEquals(1, status.piecesOnBoard());
        assertEquals(3, status.piecesInBase());
    }

    @Test
    void describesEachPieceLocationInTheBriefFormat() {
        Player red = new Player(Colour.RED, context -> context.options().get(0));
        red.pieces().get(0).enterBoard(Direction.CLOCKWISE);
        PlayerStatusDto status = snapshots.snapshotOf(red);
        assertEquals(new PieceLocationDto("R1", "26"), status.pieces().get(0));
        assertEquals(new PieceLocationDto("R2", "Base"), status.pieces().get(1));
    }
}
