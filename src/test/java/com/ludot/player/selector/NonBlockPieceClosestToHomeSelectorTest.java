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
import static com.ludot.testsupport.MoveOptions.leavingBlock;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class NonBlockPieceClosestToHomeSelectorTest {

    private final NonBlockPieceClosestToHomeSelector selector =
            new NonBlockPieceClosestToHomeSelector(new TrackNavigator(), NEXT);
    private final Piece green1 = new Piece(Colour.GREEN, 1);
    private final Piece green3 = new Piece(Colour.GREEN, 3);

    @Test
    void picksAPieceThatIsNotInABlock() {
        green1.enterBoard(Direction.CLOCKWISE);
        green3.enterBoard(Direction.CLOCKWISE);
        MoveOption free = move(green3);
        assertSame(free, selector.select(context(3, leavingBlock(green1), free)));
    }

    @Test
    void passesOnWhenOnlyBlockPiecesCanMove() {
        assertSame(PASSED_ON, selector.select(context(3, leavingBlock(green1))));
    }
}