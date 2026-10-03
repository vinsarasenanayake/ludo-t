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

    @Test
    void pieceInBaseCannotMoveWithoutASix() {
        assertTrue(planner.findOptions(List.of(red1), 5, NO_MYSTERY).isEmpty());
    }

    @Test
    void sixEntersThePieceOntoItsX() {
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Type.ENTER_BOARD, option.type());
        assertEquals(Position.onTrack(26), option.destination());
    }

    @Test
    void everyPieceInBaseMayEnter() {
        assertEquals(2, planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY).size());
    }

    @Test
    void pieceMovesByTheRollInItsDirection() {
        placeOnTrack(red1, 10, Direction.CLOCKWISE);
        placeOnTrack(red2, 40, Direction.COUNTER_CLOCKWISE);
        assertEquals(Position.onTrack(14), onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY)).destination());
        assertEquals(Position.onTrack(36), onlyOption(planner.findOptions(List.of(red2), 4, NO_MYSTERY)).destination());
    }

    @Test
    void pieceJumpsOverASingleOpponent() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(green1, 28, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(30), option.destination());
        assertFalse(option.capturesAny());
    }

    @Test
    void landingOnAnOpponentCapturesIt() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(green1, 30, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(List.of(green1), option.landing().victims());
    }

    @Test
    void landingOnOwnPieceFormsABlock() {
        board.enter(red1, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertTrue(option.formsBlock());
    }

    @Test
    void pieceStopsBeforeAnOpponentBlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(4);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(3), option.destination());
        assertTrue(option.isCutShortByBlock());
        assertFalse(option.capturesAny());
    }

    @Test
    void pieceWithoutACaptureCarriesOnPastItsApproach() {
        placeOnTrack(red1, 23, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.onTrack(26), option.destination());
    }

    @Test
    void pieceWithACaptureTurnsIntoItsHomeStraight() {
        placeOnTrack(red1, 22, Direction.CLOCKWISE);
        red1.recordCapture();
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(1), option.destination());
    }

    @Test
    void counterClockwiseFirstPassDoesNotEnterHomeStraight() {
        placeOnTrack(red1, 26, Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(22), option.destination());
    }

    @Test
    void counterClockwiseSecondPassEntersHomeStraight() {
        placeOnTrack(red1, 25, Direction.COUNTER_CLOCKWISE);
        red1.recordCapture();
        red1.recordApproachPass();
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(1), option.destination());
    }

    @Test
    void exactRollReachesHome() {
        board.move(red1, Position.inHomeStraight(2));
        assertEquals(Position.home(), onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY)).destination());
    }

    @Test
    void rollPastHomeIsNotAllowed() {
        board.move(red1, Position.inHomeStraight(2));
        assertTrue(planner.findOptions(List.of(red1), 4, NO_MYSTERY).isEmpty());
    }

    @Test
    void landingOnTheMysteryCellIsFlagged() {
        board.enter(red1, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, new MysteryCell.Active(30, 4)));
        assertTrue(option.landsOnMysteryCell());
    }

    @Test
    void blockMovesByRollDividedBySize() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        MoveOption blockMove = blockMoveIn(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(3), blockMove.destination());
        assertEquals(2, blockMove.movers().size());
    }

    @Test
    void blockMoveIgnoresPieceEffects() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Energised());
        MoveOption blockMove = blockMoveIn(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(3), blockMove.destination());
    }

    @Test
    void mixedBlockMovesTheWayOfThePieceFurthestFromHome() {
        placeOnTrack(red1, 30, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.COUNTER_CLOCKWISE);
        MoveOption blockMove = blockMoveIn(planner.findOptions(List.of(red1, red2), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(28), blockMove.destination());
    }

    @Test
    void blockCapturesSameSizeBlock() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(3);
        MoveOption blockMove = blockMoveIn(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY));
        assertEquals(2, blockMove.landing().victims().size());
    }

    @Test
    void homeStraightPieceDoesNotLeaveATrackBlock() {
        board.move(red1, Position.inHomeStraight(2));
        placeOnTrack(red2, 2, Direction.CLOCKWISE);
        placeOnTrack(new Piece(Colour.RED, 3), 2, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 1, NO_MYSTERY));
        assertFalse(option.leavesBlock());
    }

    @Test
    void blockedMoveIsOnlyOfferedAsALastResort() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeOnTrack(red2, 10, Direction.CLOCKWISE);
        placeGreenBlockAt(4);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(16), option.destination());
        assertFalse(option.isCutShortByBlock());
    }

    @Test
    void blockOfPiecesThatHaveCapturedEntersTheHomeStraight() {
        placeOnTrack(red1, 22, Direction.CLOCKWISE);
        placeOnTrack(red2, 22, Direction.CLOCKWISE);
        red1.recordCapture();
        red2.recordCapture();
        MoveOption blockMove = blockMoveIn(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(0), blockMove.destination());
    }

    @Test
    void blockWithAPieceThatNeedsACaptureStaysOnTheTrack() {
        placeOnTrack(red1, 22, Direction.CLOCKWISE);
        placeOnTrack(red2, 22, Direction.CLOCKWISE);
        red1.recordCapture();
        MoveOption blockMove = blockMoveIn(planner.findOptions(List.of(red1, red2), 6, NO_MYSTERY));
        assertEquals(Position.onTrack(25), blockMove.destination());
    }

    @Test
    void rollEndingOnAnOpponentBlockIsBlockedNotShortened() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        placeGreenBlockAt(4);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertTrue(option.isFullyBlocked());
        assertEquals(Position.onTrack(0), option.destination());
    }

    @Test
    void blockedMoveNamesTheRealIntendedDestination() {
        placeOnTrack(red1, 21, Direction.CLOCKWISE);
        red1.recordCapture();
        placeGreenBlockAt(23);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Position.inHomeStraight(2), option.route().blockage().orElseThrow().intendedDestination());
    }

    @Test
    void energisedPieceMovesDouble() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Energised());
        assertEquals(Position.onTrack(6), onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY)).destination());
    }

    @Test
    void briefedPieceHasNoMove() {
        placeOnTrack(red1, 0, Direction.CLOCKWISE);
        red1.applyEffect(new PieceEffect.Briefing());
        assertTrue(planner.findOptions(List.of(red1), 4, NO_MYSTERY).isEmpty());
    }

    @Test
    void opponentBlockOnTheStartCellStopsEntry() {
        placeGreenBlockAt(26);
        MoveOption entry = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertTrue(entry.isFullyBlocked());
        assertEquals(Position.onTrack(26), entry.route().blockage().orElseThrow().intendedDestination());
    }

    @Test
    void enteringOntoAnOwnPieceFormsABlock() {
        placeOnTrack(red2, 26, Direction.CLOCKWISE);
        MoveOption entry = onlyOption(planner.findOptions(List.of(red1), 6, NO_MYSTERY));
        assertEquals(Type.ENTER_BOARD, entry.type());
        assertTrue(entry.formsBlock());
    }

    @Test
    void counterClockwisePieceWrapsPastZero() {
        placeOnTrack(red1, 1, Direction.COUNTER_CLOCKWISE);
        assertEquals(Position.onTrack(50), onlyOption(planner.findOptions(List.of(red1), 3, NO_MYSTERY)).destination());
    }

    @Test
    void pieceAtHomeHasNoMove() {
        board.move(red1, Position.home());
        assertTrue(planner.findOptions(List.of(red1), 4, NO_MYSTERY).isEmpty());
    }

    @Test
    void pieceLeavingABlockKeepsItsOwnDirection() {
        placeOnTrack(red1, 30, Direction.CLOCKWISE);
        placeOnTrack(red2, 30, Direction.COUNTER_CLOCKWISE);
        List<MoveOption> options = planner.findOptions(List.of(red1, red2), 2, NO_MYSTERY);
        assertEquals(Position.onTrack(32), singleMoveOf(red1, options).destination());
        assertEquals(Position.onTrack(28), singleMoveOf(red2, options).destination());
    }

    @Test
    void blockWithAnotherColourOnItStillStopsOthers() {
        placeGreenBlockAt(30);
        placeOnTrack(new Piece(Colour.YELLOW, 1), 30, Direction.CLOCKWISE);
        placeOnTrack(red1, 28, Direction.CLOCKWISE);
        MoveOption option = onlyOption(planner.findOptions(List.of(red1), 4, NO_MYSTERY));
        assertEquals(Position.onTrack(29), option.destination());
    }

    @Test
    void blockMoveTakesOnlyOwnPieces() {
        placeOnTrack(red1, 10, Direction.CLOCKWISE);
        placeOnTrack(red2, 10, Direction.CLOCKWISE);
        placeOnTrack(green1, 10, Direction.CLOCKWISE);
        MoveOption blockMove = blockMoveIn(planner.findOptions(List.of(red1, red2), 4, NO_MYSTERY));
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
        return options.getFirst();
    }

    private MoveOption blockMoveIn(List<MoveOption> options) {
        return options.stream()
                .filter(option -> option.type() == Type.MOVE_BLOCK)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no block move in " + options));
    }
}