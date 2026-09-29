package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.move;
import static com.ludot.testsupport.MoveOptions.ontoMystery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CyclicMysteryStrategyTest {

    private CyclicMysteryStrategy blue;
    private Piece blue1;
    private Piece blue2;
    private Piece blue3;

    @BeforeEach
    void setUp() {
        blue = new CyclicMysteryStrategy();
        blue1 = new Piece(Colour.BLUE, 1);
        blue2 = new Piece(Colour.BLUE, 2);
        blue3 = new Piece(Colour.BLUE, 3);
        blue1.enterBoard(Direction.CLOCKWISE);
        blue2.enterBoard(Direction.CLOCKWISE);
        blue3.enterBoard(Direction.COUNTER_CLOCKWISE);
    }

    @Test
    @DisplayName("Blue starts the cycle with B1")
    void startsWithPieceOne() {
        MoveOption first = move(blue1);
        assertSame(first, blue.chooseMove(context(3, move(blue2), first)));
    }

    @Test
    @DisplayName("Blue moves in a cycle: after B1 comes B2")
    void movesToTheNextPieceInTheCycle() {
        blue.chooseMove(context(3, move(blue1), move(blue2)));
        MoveOption second = move(blue2);
        assertSame(second, blue.chooseMove(context(3, move(blue1), second)));
    }

    @Test
    @DisplayName("Assumption A16: an unmovable piece is skipped")
    void skipsAPieceWithNoMoves() {
        blue.chooseMove(context(3, move(blue1)));
        MoveOption third = move(blue3);
        assertSame(third, blue.chooseMove(context(3, move(blue1), third)));
        assertEquals(4, blue.nextPieceNumber());
    }

    @Test
    @DisplayName("Blue moving counter-clockwise prefers landing on the mystery cell")
    void counterClockwisePieceSeeksTheMysteryCell() {
        blue.chooseMove(context(3, move(blue1)));
        blue.chooseMove(context(3, move(blue2)));
        MoveOption mystery = ontoMystery(blue3);
        assertSame(mystery, blue.chooseMove(context(3, move(blue3), mystery)));
    }

    @Test
    @DisplayName("Blue moving clockwise avoids landing on the mystery cell")
    void clockwisePieceAvoidsTheMysteryCell() {
        MoveOption safe = move(blue1);
        assertSame(safe, blue.chooseMove(context(3, ontoMystery(blue1), safe)));
    }
}