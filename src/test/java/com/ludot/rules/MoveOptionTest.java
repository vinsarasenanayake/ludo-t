package com.ludot.rules;

import com.ludot.board.Colour;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;
import com.ludot.rules.MoveOption.Landing;
import com.ludot.rules.MoveOption.Type;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveOptionTest {

    private final Piece red1 = new Piece(Colour.RED, 1);
    private final Piece green1 = new Piece(Colour.GREEN, 1);

    // T-3 + A9: a piece that cannot move even one cell is fully blocked
    @Test
    void zeroDistanceBlockedRouteIsFullyBlocked() {
        MoveOption stuck = optionWith(blockedRoute(0), Landing.offTrack());
        MoveOption partial = optionWith(blockedRoute(2), Landing.offTrack());
        assertTrue(stuck.isFullyBlocked());
        assertTrue(partial.isCutShortByBlock());
        assertFalse(partial.isFullyBlocked());
    }

    // R6: an option captures when its landing has a victim
    @Test
    void capturesWhenTheLandingHasAVictim() {
        Route route = completedRoute();
        assertTrue(optionWith(route, new Landing(List.of(green1), false, false)).capturesAny());
        assertFalse(optionWith(route, Landing.offTrack()).capturesAny());
    }

    // Design: the lead piece and destination come from the movers and the route
    @Test
    void leadPieceAndDestinationComeFromMoversAndRoute() {
        Route route = completedRoute();
        MoveOption option = optionWith(route, Landing.offTrack());
        assertEquals(red1, option.leadPiece());
        assertEquals(Position.onTrack(4), option.destination());
    }

    private Route completedRoute() {
        return Route.completed(Position.onTrack(0), Position.onTrack(4), 4, 0);
    }

    private Route blockedRoute(int distance) {
        return Route.completed(Position.onTrack(0), Position.onTrack(distance), distance, 0)
                .stoppedBy(new Route.Blockage(Position.onTrack(6), green1));
    }

    private MoveOption optionWith(Route route, Landing landing) {
        return new MoveOption(Type.MOVE_PIECE, List.of(red1), route, landing, false);
    }
}
