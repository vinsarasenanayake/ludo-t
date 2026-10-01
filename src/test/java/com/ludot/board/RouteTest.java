package com.ludot.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteTest {

    // T-3: a route stopped by a block keeps where it stopped and remembers the blocker
    @Test
    void stoppedRouteKeepsTheBlockDetails() {
        Piece blocker = new Piece(Colour.GREEN, 1);
        Route route = Route.completed(Position.onTrack(0), Position.onTrack(3), 3, 0)
                .stoppedBy(new Route.Blockage(Position.onTrack(6), blocker));
        assertTrue(route.isCutShortByBlock());
        assertEquals(Position.onTrack(3), route.destination());
        assertEquals(blocker, route.blockage().orElseThrow().blockingPiece());
    }
}
