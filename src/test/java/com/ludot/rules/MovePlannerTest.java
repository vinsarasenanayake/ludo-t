package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.PieceEffect;
import com.ludot.domain.Position;
import com.ludot.rules.MoveOption.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovePlannerTest {

    private static final MysteryCell NO_MYSTERY = MysteryCell.None.INSTANCE;

    private Board board;
    private MovePlanner planner;
    private Piece red1;
    private Piece red2;
    private Piece green1;
    private Piece green2;

    @BeforeEach
    void setUp() {
        board = new Board();
        planner = new MovePlanner(board, new TrackNavigator());
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
        green1 = new Piece(Colour.GREEN, 1);
        green2 = new Piece(Colour.GREEN, 2);
    }

    @Test
    @DisplayName("Rule 2: a piece cannot leave base without a six")
    void noOptionToLeaveBaseWithoutSix() {
        List<MoveOption> options = planner.findOptions(List.of(red1), 5, NO_MYSTERY);
        assertTrue(options.isEmpty());
    }

    @Test
    @DisplayName("Rule 2: a six lets a piece enter the board at its X")
    void sixOffersEntryToTheStartCell() {
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Type.ENTER_BOARD, option.type());
        assertEquals(Position.onTrack(26), option.destination());
    }

    @Test
    @DisplayName("Rule 1: a piece moves forward by the roll")
    void pieceMovesByTheRoll() {
        board.enter(red1, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(30), option.destination());
    }

    @Test
    @DisplayName("Rule T-1: a counter-clockwise piece moves backwards around the track")
    void counterClockwisePieceMovesTheOtherWay() {
        board.enter(red1, Direction.COUNTER_CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(22), option.destination());
    }

    @Test
    void movementWrapsFromCellFiftyOneToZero() {
        placeOnTrack(red1, 50, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.onTrack(1), option.destination());
    }

    @Test
    @DisplayName("Rule 5: a piece can jump over a single opponent piece")
    void pieceJumpsOverASingleOpponent() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(green1, 28, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(30), option.destination());
        assertFalse(option.capturesAny());
    }

    @Test
    @DisplayName("Rule 6: landing on an opponent piece captures it")
    void landingOnAnOpponentCapturesIt() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(green1, 30, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(List.of(green1), option.landing().victims());
    }

    @Test
    @DisplayName("Rule T-3: landing on an own piece forms a block")
    void landingOnOwnPieceFormsABlock() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.CLOCKWISE);
        MoveOption option = planner.findOptions(List.of(red1), 4, NO_MYSTERY).get(0);
        assertTrue(option.formsBlock());
    }

    @Test
    @DisplayName("Rule T-3: a piece stops on the cell before an opponent block")
    void pieceStopsBeforeAnOpponentBlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(4);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(3), option.destination());
        assertTrue(option.isCutShortByBlock());
    }

    @Test
    @DisplayName("Rule T-3: no move at all when the block is on the very next cell")
    void noMoveWhenBlockIsAdjacent() {
        placeOnTrack(red1, 3, Direction.CLOCKWISE);
        placeGreenBlockAt(4);
        assertTrue(planner.findOptions(List.of(red1), 5, NO_MYSTERY).isEmpty());
    }

    @Test
    @DisplayName("Rule T-7: without a capture the piece passes its approach and keeps circling")
    void pieceWithoutCaptureStaysOnTheTrack() {
        placeOnTrack(red1, 23, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.onTrack(26), option.destination());
    }

    @Test
    @DisplayName("Rule 9 + T-7: after a capture the piece turns into its home straight")
    void pieceWithCaptureEntersHomeStraight() {
        placeOnTrack(red1, 23, Direction.CLOCKWISE);
        red1.recordCapture();
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(1), option.destination());
    }

    @Test
    @DisplayName("Rule T-1: counter-clockwise, the first pass of the approach does not count")
    void counterClockwiseFirstPassKeepsCircling() {
        board.enter(red1, Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(22), option.destination());
    }

    @Test
    @DisplayName("Rule T-1: counter-clockwise, the second pass enters the home straight")
    void counterClockwiseSecondPassEntersHomeStraight() {
        placeOnTrack(red1, 25, Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        red1.recordApproachPass();
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(1), option.destination());
    }

    @Test
    @DisplayName("Rule 10: the exact roll takes a piece home")
    void exactRollReachesHome() {
        red1.moveTo(Position.inHomeStraight(2));
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.home(), option.destination());
    }

    @Test
    @DisplayName("Rule 10: a roll that overshoots home is not allowed")
    void overshootingHomeIsNotAllowed() {
        red1.moveTo(Position.inHomeStraight(2));
        assertTrue(planner.findOptions(List.of(red1), 4, NO_MYSTERY).isEmpty());
    }

    @Test
    @DisplayName("Rule T-12: an energised piece moves double")
    void energisedPieceMovesDouble() {
        board.enter(red1, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Energised());
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.onTrack(32), option.destination());
    }

    @Test
    @DisplayName("Rule T-13: a piece at a briefing has no moves")
    void briefedPieceHasNoMoves() {
        board.enter(red1, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Briefing());
        assertTrue(planner.findOptions(List.of(red1), 3, NO_MYSTERY).isEmpty());
    }

    @Test
    @DisplayName("Rule T-10: landing on the mystery cell is flagged")
    void landingOnTheMysteryCellIsFlagged() {
        board.enter(red1, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, new MysteryCell.Active(30, 4)));
        assertTrue(option.landsOnMysteryCell());
    }

    @Test
    @DisplayName("Rule T-4: a block moves by the roll divided by its size")
    void blockMovesByRollDividedBySize() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(Position.onTrack(3), blockMove.destination());
        assertEquals(2, blockMove.movers().size());
    }

    @Test
    @DisplayName("Rule T-4: an opposite-direction block moves the way of the piece furthest from home")
    void mixedBlockMovesTheWayOfThePieceFurthestFromHome() {
        placeOnTrack(red1, 30, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.COUNTER_CLOCKWISE);
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 4, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(Position.onTrack(28), blockMove.destination());
    }

    @Test
    @DisplayName("Rule T-8: a block can capture a block of the same size")
    void blockCapturesSameSizeBlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(3);
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(2, blockMove.landing().victims().size());
    }

    @Test
    void movingOnePieceOutOfABlockIsMarkedAsLeavingTheBlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        MoveOption single = optionOfType(planner.findOptions(List.of(red1, red2), 5, NO_MYSTERY), Type.MOVE_PIECE);
        assertTrue(single.leavesBlock());
    }

    @Test
    @DisplayName("A piece in its home straight is never treated as leaving a block on the track")
    void homeStraightPieceDoesNotLeaveATrackBlock() {
        red1.moveTo(Position.inHomeStraight(2));
        placeGreenBlockAt(2);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 1, NO_MYSTERY));
        assertFalse(option.leavesBlock());
    }

    @Test
    @DisplayName("Rule T-3: one piece cannot capture a block, it stops in front of it")
    void singlePieceCannotCaptureABlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(3);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertFalse(option.capturesAny());
        assertEquals(Position.onTrack(2), option.destination());
    }

    @Test
    void leadPieceIsTheFirstMover() {
        board.enter(red1, Direction.CLOCKWISE);
        assertSame(red1, onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY)).leadPiece());
    }

    private void placeOnTrack(Piece piece, int cell, Direction direction) {
        board.enter(piece, direction);
        board.move(piece, Position.onTrack(cell));
    }

    private void placeGreenBlockAt(int cell) {
        placeOnTrack(green1, cell, Direction.CLOCKWISE);
        placeOnTrack(green2, cell, Direction.CLOCKWISE);
    }

    private MoveOption onlyOption(List<MoveOption> options) {
        assertEquals(1, options.size(), "expected exactly one option but got " + options);
        return options.get(0);
    }

    private MoveOption optionOfType(List<MoveOption> options, Type type) {
        return options.stream()
                .filter(option -> option.type() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no " + type + " option in " + options));
    }
}
