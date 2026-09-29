package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;

import static com.ludot.testsupport.MoveOptions.move;

final class SelectorTestSupport {

    static final MoveOption PASSED_ON = move(new Piece(Colour.BLUE, 4));
    static final MoveSelector NEXT = context -> PASSED_ON;

    private SelectorTestSupport() {
    }
}
