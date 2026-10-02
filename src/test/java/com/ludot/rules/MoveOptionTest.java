package com.ludot.rules;

import com.ludot.board.Colour;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;
import com.ludot.rules.MoveOption.Landing;
import com.ludot.rules.MoveOption.Type;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveOptionTest {

    private final Piece red1 = new Piece(Colour.RED, 1);
    private final Piece green1 = new Piece(Colour.GREEN, 1);

    // T-3: no step taken is fully blocked
    @Test
    void zeroDistanceBlockedRouteIsFullyBlocked() {
        assertTrue(optionWith(blockedRoute(0), Landing.offTrack()).isFullyBlocked());
    }

    // T-3: part of the way is cut short
    @Test
    void partlyBlockedRouteIsCutShortButNotFullyBlocked() {
        MoveOption partial = optionWith(blockedRoute(2), Landing.offTrack());
        assertTrue(partial.isCutShortByBlock());
        assertFalse(partial.isFullyBlocked());
    }

    private Route blockedRoute(int distance) {
        return Route.completed(Position.onTrack(0), Position.onTrack(distance), distance, 0)
                .stoppedBy(new Route.Blockage(Position.onTrack(6), green1));
    }

    private MoveOption optionWith(Route route, Landing landing) {
        return new MoveOption(Type.MOVE_PIECE, List.of(red1), route, landing, false);
    }
}