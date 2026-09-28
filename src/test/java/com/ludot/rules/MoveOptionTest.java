package com.ludot.rules;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveOptionTest {

    private final Piece red1 = new Piece(Colour.RED, 1);
    private final Piece green1 = new Piece(Colour.GREEN, 1);

    @Test
    void leadPieceIsTheFirstMover() {
        MoveOption option = optionWith(Landing.offTrack(), Route.completed(Position.onTrack(1), Position.onTrack(4), 3, 0));
        assertSame(red1, option.leadPiece());
    }

    @Test
    void optionWithVictimsCaptures() {
        Landing landing = new Landing(List.of(green1), false, false);
        assertTrue(optionWith(landing, Route.completed(Position.onTrack(1), Position.onTrack(4), 3, 0)).capturesAny());
    }

    @Test
    void completedRouteIsNotCutShort() {
        Route route = Route.completed(Position.onTrack(1), Position.onTrack(4), 3, 0);
        assertFalse(route.isCutShortByBlock());
    }

    @Test
    void routeStoppedBeforeABlockIsCutShort() {
        Blockage blockage = new Blockage(Position.onTrack(6), green1);
        Route route = Route.cutShort(Position.onTrack(1), Position.onTrack(3), 2, 0, blockage);
        assertTrue(route.isCutShortByBlock());
    }

    @Test
    void destinationComesFromTheRoute() {
        MoveOption option = optionWith(Landing.offTrack(), Route.completed(Position.onTrack(1), Position.onTrack(4), 3, 0));
        assertEquals(Position.onTrack(4), option.destination());
    }

    private MoveOption optionWith(Landing landing, Route route) {
        return new MoveOption(MoveType.MOVE_PIECE, List.of(red1), route, landing, false);
    }
}