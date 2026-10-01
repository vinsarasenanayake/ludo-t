package com.ludot.player;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCell;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MovePlanner;

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
    private final Piece blue4 = onBoard(4, Direction.COUNTER_CLOCKWISE);

    // Blue (2.1.4): B1 is moved in the first round and B2 is considered in the next
    @Test
    void movesTheNextPieceInTheNextRound() {
        MoveOption first = move(blue1);
        MoveOption second = move(blue2);
        assertSame(first, blue.chooseMove(List.of(second, first)));
        blue.onRoundEnded();
        assertSame(second, blue.chooseMove(List.of(move(blue1), second)));
    }

    // Blue (2.1.4): a bonus roll in the same round keeps to the piece scheduled for that round
    @Test
    void bonusRollInTheSameRoundKeepsTheScheduledPiece() {
        MoveOption first = move(blue1);
        blue.chooseMove(List.of(first, move(blue2)));
        assertSame(first, blue.chooseMove(List.of(move(blue2), first)));
    }

    // Blue (2.1.4) (interpretation): a scheduled piece that cannot move is skipped for the next one in the cycle
    @Test
    void skipsAPieceWithNoMove() {
        MoveOption third = move(blue3);
        assertSame(third, blue.chooseMove(List.of(move(blue4), third)));
    }

    // Blue (2.1.4): after B4 the cycle starts again at B1
    @Test
    void cycleWrapsRoundFromB4ToB1() {
        endRounds(3);
        MoveOption wrapped = move(blue1);
        assertSame(wrapped, blue.chooseMove(List.of(move(blue2), wrapped)));
    }

    // Blue (2.1.4): a clockwise piece that would land on the mystery cell gives way to another piece
    @Test
    void clockwisePieceAvoidsTheMysteryCell() {
        MoveOption safe = move(blue2);
        assertSame(safe, blue.chooseMove(List.of(ontoMystery(blue1), safe)));
    }

    // Blue (2.1.4): with real planner options, a counter-clockwise B3 gives way to a counter-clockwise B4
    // that lands on the mystery cell
    @Test
    void plannedCounterClockwiseMoveOntoTheMysteryCellIsPreferred() {
        board.move(blue3, Position.onTrack(40));
        board.move(blue4, Position.onTrack(10));
        MovePlanner planner = new MovePlanner(board, new TrackNavigator());
        List<MoveOption> options = planner.findOptions(List.of(blue3, blue4), 4, new MysteryCell.Active(6, 4));
        endRounds(2);
        assertSame(blue4, blue.chooseMove(options).leadPiece());
    }

    private void endRounds(int rounds) {
        for (int round = 0; round < rounds; round++) {
            blue.onRoundEnded();
        }
    }

    private Piece onBoard(int number, Direction direction) {
        Piece piece = new Piece(Colour.BLUE, number);
        board.enter(piece, direction);
        return piece;
    }
}
