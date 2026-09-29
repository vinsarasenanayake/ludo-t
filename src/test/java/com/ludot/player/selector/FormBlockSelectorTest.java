package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import org.junit.jupiter.api.Test;

import static com.ludot.player.selector.SelectorTestSupport.NEXT;
import static com.ludot.player.selector.SelectorTestSupport.PASSED_ON;
import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.formingBlock;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class FormBlockSelectorTest {

    private final FormBlockSelector selector = new FormBlockSelector(NEXT);
    private final Piece green1 = new Piece(Colour.GREEN, 1);

    @Test
    void picksTheOptionThatFormsABlock() {
        MoveOption blockMaker = formingBlock(green1);
        assertSame(blockMaker, selector.select(context(move(green1), blockMaker)));
    }

    @Test
    void passesOnWhenNoOptionFormsABlock() {
        assertSame(PASSED_ON, selector.select(context(move(green1))));
    }
}
