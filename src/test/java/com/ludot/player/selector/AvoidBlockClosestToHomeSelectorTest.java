package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;
import org.junit.jupiter.api.Test;

import static com.ludot.player.selector.SelectorTestSupport.NEXT;
import static com.ludot.player.selector.SelectorTestSupport.PASSED_ON;
import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.formingBlock;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class AvoidBlockClosestToHomeSelectorTest {

    private final AvoidBlockClosestToHomeSelector selector =
            new AvoidBlockClosestToHomeSelector(new TrackNavigator(), NEXT);
    private final Piece red1 = new Piece(Colour.RED, 1);
    private final Piece red2 = new Piece(Colour.RED, 2);

    @Test
    void picksAMoveThatDoesNotFormABlock() {
        red1.enterBoard(Direction.CLOCKWISE);
        red2.enterBoard(Direction.CLOCKWISE);
        MoveOption plain = move(red2);
        assertSame(plain, selector.select(context(formingBlock(red1), plain)));
    }

    @Test
    void passesOnWhenEveryMoveFormsABlock() {
        assertSame(PASSED_ON, selector.select(context(formingBlock(red1))));
    }
}
