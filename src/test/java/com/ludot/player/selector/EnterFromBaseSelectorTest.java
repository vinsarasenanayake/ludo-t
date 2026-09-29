package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import org.junit.jupiter.api.Test;

import static com.ludot.player.selector.SelectorTestSupport.NEXT;
import static com.ludot.player.selector.SelectorTestSupport.PASSED_ON;
import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.enter;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class EnterFromBaseSelectorTest {

    private final EnterFromBaseSelector selector = new EnterFromBaseSelector(NEXT);
    private final Piece red1 = new Piece(Colour.RED, 1);

    @Test
    void picksTheEntryOption() {
        MoveOption entry = enter(red1);
        assertSame(entry, selector.select(context(6, move(red1), entry)));
    }

    @Test
    void passesOnWhenThereIsNoEntry() {
        assertSame(PASSED_ON, selector.select(context(3, move(red1))));
    }
}