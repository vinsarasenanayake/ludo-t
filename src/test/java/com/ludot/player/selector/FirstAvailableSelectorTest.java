package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import org.junit.jupiter.api.Test;

import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class FirstAvailableSelectorTest {

    @Test
    void alwaysReturnsTheFirstOption() {
        MoveOption first = move(new Piece(Colour.RED, 1));
        MoveOption second = move(new Piece(Colour.RED, 2));
        assertSame(first, new FirstAvailableSelector().select(context(first, second)));
    }
}
