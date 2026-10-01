package com.ludot.rules;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.PieceEffect;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCell;
import com.ludot.rules.MoveOption.Type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    // R2: only a six lets a piece leave base, and every piece in base may be the one to enter at X
    @Test
    void onlyASixEntersTheBoard() {
        assertTrue(planner.findOptions(List.of(red1), 5, NO_MYSTERY).isEmpty());
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Type.ENTER_BOARD, option.type());
        assertEquals(Position.onTrack(26), option.destination());
        assertEquals(2, planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY).size());
    }

    // R1 + T-1: a piece moves by the roll in its own direction
    @Test
    void pieceMovesByTheRollInItsDirection() {
        placeOnTrack(red1, 10, Direction.CLOCKWISE);
        placeOnTrack(red2, 40, Direction.COUNTER_CLOCKWISE);
        assertEquals(Position.onTrack(14), onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY)).destination());
        assertEquals(Position.onTrack(36), onlyOption(planner.findOptions(List.of(red2), 4, NO_MYSTERY)).destination());
    }

    // R5: a piece can jump over a single opponent piece
    @Test
    void pieceJumpsOverASingleOpponent() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(green1, 28, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(30), option.destination());
        assertFalse(option.capturesAny());
    }

    // R6: landing on an opponent piece captures it
    @Test
    void landingOnAnOpponentCapturesIt() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(green1, 30, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(List.of(green1), option.landing().victims());
    }

    // R7 + T-3: landing on an own piece is allowed in LUDO-T and forms a block
    @Test
    void landingOnOwnPieceFormsABlock() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertTrue(option.formsBlock());
    }

    // T-3: a single piece cannot pass or capture a block, it stops in front of it
    @Test
    void pieceStopsBeforeAnOpponentBlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(4);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(3), option.destination());
        assertTrue(option.isCutShortByBlock());
        assertFalse(option.capturesAny());
    }

    // R9 + T-7: only a piece that has captured turns into its home straight
    @Test
    void onlyAPieceWithACaptureEntersHomeStraight() {
        placeOnTrack(red1, 23, Direction.CLOCKWISE);
        placeOnTrack(red2, 22, Direction.CLOCKWISE);
        red2.recordCapture();
        assertEquals(Position.onTrack(26), onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY)).destination());
        MoveOption afterCapture = onlyOption(planner.findOptions(List.of(red2), 4, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(1), afterCapture.destination());
    }

    // T-1: counter-clockwise, only the second pass of the approach enters the home straight
    @Test
    void counterClockwiseSecondPassEntersHomeStraight() {
        placeOnTrack(red1, 25, Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        red1.recordApproachPass();
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(1), option.destination());
        placeOnTrack(red2, 26, Direction.COUNTER_CLOCKWISE);
        red2.recordCapture();
        MoveOption firstPass = onlyOption(planner.findOptions(List.of(red2), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(22), firstPass.destination());
    }

    // R10: only the exact roll takes a piece home
    @Test
    void onlyTheExactRollReachesHome() {
        red1.moveTo(Position.inHomeStraight(2));
        assertEquals(Position.home(), onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY)).destination());
        assertTrue(planner.findOptions(List.of(red1), 4, NO_MYSTERY).isEmpty());
    }

    // T-11: landing on the mystery cell is flagged
    @Test
    void landingOnTheMysteryCellIsFlagged() {
        board.enter(red1, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, new MysteryCell.Active(30, 4)));
        assertTrue(option.landsOnMysteryCell());
    }

    // T-4: a block moves by the roll divided by its size
    @Test
    void blockMovesByRollDividedBySize() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(Position.onTrack(3), blockMove.destination());
        assertEquals(2, blockMove.movers().size());
    }

    // T-4: an opposite-direction block moves the way of the piece furthest from home
    @Test
    void mixedBlockMovesTheWayOfThePieceFurthestFromHome() {
        placeOnTrack(red1, 30, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.COUNTER_CLOCKWISE);
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 4, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(Position.onTrack(28), blockMove.destination());
    }

    // T-8: a block can capture a block of the same size
    @Test
    void blockCapturesSameSizeBlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(3);
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(2, blockMove.landing().victims().size());
    }

    // Design: a home straight cell is never mistaken for the track cell with the same number
    @Test
    void homeStraightPieceDoesNotLeaveATrackBlock() {
        red1.moveTo(Position.inHomeStraight(2));
        placeOnTrack(red2, 2, Direction.CLOCKWISE);
        placeOnTrack(new Piece(Colour.RED, 3), 2, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 1, NO_MYSTERY));
        assertFalse(option.leavesBlock());
    }

    // T-3 + A9: a move cut short by a block is only offered when no other move exists
    @Test
    void blockedMoveIsOnlyOfferedAsALastResort() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 10, Direction.CLOCKWISE);
        placeGreenBlockAt(4);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(16), option.destination());
        assertFalse(option.isCutShortByBlock());
    }

    // A7: a block never enters the home straight, it carries on along the track
    @Test
    void blockNeverEntersTheHomeStraight() {
        placeOnTrack(red1, 22, Direction.CLOCKWISE);
        placeOnTrack(red2, 22, Direction.CLOCKWISE);
        red1.recordCapture();
        red2.recordCapture();
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(Position.onTrack(25), blockMove.destination());
    }

    // T-12: an energised piece moves double the roll
    @Test
    void energisedPieceMovesDouble() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Energised());
        assertEquals(Position.onTrack(6), onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY)).destination());
    }

    // T-12 + A1: a sick piece moves half the roll, rounded down
    @Test
    void sickPieceMovesHalf() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Sick());
        assertEquals(Position.onTrack(2), onlyOption(planner.findOptions(List.of(red1), 5, NO_MYSTERY)).destination());
    }

    // T-13: a piece in a briefing has no move at all
    @Test
    void briefedPieceHasNoMove() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Briefing());
        assertTrue(planner.findOptions(List.of(red1), 4, NO_MYSTERY).isEmpty());
    }

    // T-3: a piece cannot enter while an opponent block sits on its X
    @Test
    void opponentBlockOnTheStartCellStopsEntry() {
        placeGreenBlockAt(26);
        assertTrue(planner.findOptions(List.of(red1), 6, NO_MYSTERY).isEmpty());
    }

    // T-3: entering onto an own piece on X forms a block
    @Test
    void enteringOntoAnOwnPieceFormsABlock() {
        placeOnTrack(red2, 26, Direction.CLOCKWISE);
        MoveOption entry = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Type.ENTER_BOARD, entry.type());
        assertTrue(entry.formsBlock());
    }

    // T-1 + R8: a counter-clockwise piece wraps from cell 0 to cell 51
    @Test
    void counterClockwisePieceWrapsPastZero() {
        placeOnTrack(red1, 1, Direction.COUNTER_CLOCKWISE);
        assertEquals(Position.onTrack(50), onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY)).destination());
    }

    // R10: a piece that has reached home has no move left
    @Test
    void pieceAtHomeHasNoMove() {
        red1.moveTo(Position.home());
        assertTrue(planner.findOptions(List.of(red1), 4, NO_MYSTERY).isEmpty());
    }

    // T-5: a piece breaking away from a block moves in its own original direction
    @Test
    void pieceLeavingABlockKeepsItsOwnDirection() {
        placeOnTrack(red1, 30, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.COUNTER_CLOCKWISE);
        List<MoveOption> options = planner.findOptions(List.of(red1, red2), 2, NO_MYSTERY);
        assertEquals(Position.onTrack(32), singleMoveOf(red1, options).destination());
        assertEquals(Position.onTrack(28), singleMoveOf(red2, options).destination());
    }

    // T-3: an opponent piece sharing a block's cell does not open the way past the block
    @Test
    void blockWithAnotherColourOnItStillStopsOthers() {
        placeGreenBlockAt(30);
        placeOnTrack(new Piece(Colour.YELLOW, 1), 30, Direction.CLOCKWISE);
        placeOnTrack(red1, 28, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(29), option.destination());
    }

    // T-4: a block move takes only the player's own pieces from a shared cell
    @Test
    void blockMoveTakesOnlyOwnPieces() {
        placeOnTrack(red1, 10, Direction.CLOCKWISE);
        placeOnTrack(red2, 10, Direction.CLOCKWISE);
        placeOnTrack(green1, 10, Direction.CLOCKWISE);
        MoveOption blockMove = optionOfType(planner.findOptions(List.of(red1, red2), 4, NO_MYSTERY), Type.MOVE_BLOCK);
        assertEquals(List.of(red1, red2), blockMove.movers());
    }

    private void placeOnTrack(Piece piece, int cell, Direction direction) {
        board.enter(piece, direction);
        board.move(piece, Position.onTrack(cell));
    }

    private void placeGreenBlockAt(int cell) {
        placeOnTrack(green1, cell, Direction.CLOCKWISE);
        placeOnTrack(green2, cell, Direction.CLOCKWISE);
    }

    private MoveOption singleMoveOf(Piece piece, List<MoveOption> options) {
        return options.stream()
                .filter(option -> option.type() == Type.MOVE_PIECE && option.leadPiece() == piece)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no single move for " + piece + " in " + options));
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
