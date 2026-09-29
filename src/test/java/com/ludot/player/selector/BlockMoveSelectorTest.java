package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import org.junit.jupiter.api.Test;

import static com.ludot.player.selector.SelectorTestSupport.NEXT;
import static com.ludot.player.selector.SelectorTestSupport.PASSED_ON;
import static com.ludot.testsupport.MoveOptions.blockMove;
import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class BlockMoveSelectorTest {

    private final BlockMoveSelector selector = new BlockMoveSelector(NEXT);
    private final Piece green1 = new Piece(Colour.GREEN, 1);
    private final Piece green2 = new Piece(Colour.GREEN, 2);

    @Test
    void picksTheBlockMove() {
        MoveOption together = blockMove(green1, green2);
        assertSame(together, selector.select(context(4, move(green1), together)));
    }

    @Test
    void passesOnWhenThereIsNoBlock() {
        assertSame(PASSED_ON, selector.select(context(4, move(green1))));
    }
}