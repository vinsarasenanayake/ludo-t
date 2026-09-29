package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;
import org.junit.jupiter.api.Test;

import static com.ludot.player.selector.SelectorTestSupport.NEXT;
import static com.ludot.player.selector.SelectorTestSupport.PASSED_ON;
import static com.ludot.testsupport.MoveOptions.capture;
import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class CaptureClosestToVictimHomeSelectorTest {

    private final CaptureClosestToVictimHomeSelector selector =
            new CaptureClosestToVictimHomeSelector(new TrackNavigator(), NEXT);
    private final Piece red1 = new Piece(Colour.RED, 1);
    private final Piece red2 = new Piece(Colour.RED, 2);
    private final Piece green1 = new Piece(Colour.GREEN, 1);
    private final Piece green2 = new Piece(Colour.GREEN, 2);

    @Test
    void picksTheVictimNearestItsHome() {
        green1.enterBoard(Direction.CLOCKWISE);
        green2.moveTo(Position.inHomeStraight(4));
        MoveOption nearHome = capture(red2, green2);
        assertSame(nearHome, selector.select(context(capture(red1, green1), nearHome)));
    }

    @Test
    void passesOnWhenNothingCanBeCaptured() {
        assertSame(PASSED_ON, selector.select(context(move(red1))));
    }
}
