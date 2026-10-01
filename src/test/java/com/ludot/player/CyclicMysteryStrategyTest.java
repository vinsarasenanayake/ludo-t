package com.ludot.player;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.rules.MoveOption;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.player.MoveOptions.move;
import static com.ludot.player.MoveOptions.ontoMystery;
import static org.junit.jupiter.api.Assertions.assertSame;

class CyclicMysteryStrategyTest {

    private final Board board = new Board();
    private final CyclicMysteryStrategy blue = new CyclicMysteryStrategy();
    private final Piece blue1 = onBoard(1, Direction.CLOCKWISE);
    private final Piece blue2 = onBoard(2, Direction.CLOCKWISE);
    private final Piece blue3 = onBoard(3, Direction.COUNTER_CLOCKWISE);
    private final Piece blue4 = onBoard(4, Direction.CLOCKWISE);

    // Blue (2.1.4): moves its pieces in a cycle, B1 then B2
    @Test
    void movesItsPiecesInACycle() {
        MoveOption first = move(blue1);
        MoveOption second = move(blue2);
        assertSame(first, blue.chooseMove(List.of(second, first)));
        assertSame(second, blue.chooseMove(List.of(move(blue1), second)));
    }

    // Blue (2.1.4) + A4: a piece that cannot move is skipped
    @Test
    void skipsAPieceWithNoMove() {
        blue.chooseMove(List.of(move(blue1)));
        MoveOption third = move(blue3);
        assertSame(third, blue.chooseMove(List.of(move(blue1), third)));
    }

    // Blue (2.1.4): after B4 the cycle starts again at B1
    @Test
    void cycleWrapsRoundFromB4ToB1() {
        blue.chooseMove(List.of(move(blue4)));
        MoveOption wrapped = move(blue1);
        assertSame(wrapped, blue.chooseMove(List.of(move(blue2), wrapped)));
    }

    // Blue (2.1.4): a clockwise piece avoids the mystery cell
    @Test
    void clockwisePieceAvoidsTheMysteryCell() {
        MoveOption safe = move(blue1);
        assertSame(safe, blue.chooseMove(List.of(ontoMystery(blue1), safe)));
    }

    // Blue (2.1.4): a counter-clockwise piece seeks the mystery cell
    @Test
    void counterClockwisePieceSeeksTheMysteryCell() {
        MoveOption mystery = ontoMystery(blue3);
        assertSame(mystery, blue.chooseMove(List.of(move(blue3), mystery)));
    }

    private Piece onBoard(int number, Direction direction) {
        Piece piece = new Piece(Colour.BLUE, number);
        board.enter(piece, direction);
        return piece;
    }
}
