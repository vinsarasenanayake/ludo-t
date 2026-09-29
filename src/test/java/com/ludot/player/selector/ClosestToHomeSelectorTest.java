package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;
import org.junit.jupiter.api.Test;

import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class ClosestToHomeSelectorTest {

    private final ClosestToHomeSelector selector =
            new ClosestToHomeSelector(new TrackNavigator(), SelectorTestSupport.NEXT);

    @Test
    void picksThePieceClosestToHome() {
        Piece yellow1 = new Piece(Colour.YELLOW, 1);
        Piece yellow2 = new Piece(Colour.YELLOW, 2);
        yellow1.enterBoard(Direction.CLOCKWISE);
        yellow2.moveTo(Position.inHomeStraight(1));
        MoveOption nearHome = move(yellow2);
        assertSame(nearHome, selector.select(context(move(yellow1), nearHome)));
    }
}
