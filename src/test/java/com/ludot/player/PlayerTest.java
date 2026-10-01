package com.ludot.player;

import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;
import com.ludot.board.TrackNavigator;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveOption.Landing;
import com.ludot.rules.MoveOption.Type;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    private final PlayerFactory factory = new PlayerFactory(new TrackNavigator());
    private final Player red = factory.createPlayer(Colour.RED);
    private final Player green = factory.createPlayer(Colour.GREEN);
    private final Player yellow = factory.createPlayer(Colour.YELLOW);
    private final Player blue = factory.createPlayer(Colour.BLUE);

    private final Piece red1 = onBoard(Colour.RED, 1, Direction.CLOCKWISE);
    private final Piece red2 = onBoard(Colour.RED, 2, Direction.CLOCKWISE);
    private final Piece green1 = onBoard(Colour.GREEN, 1, Direction.CLOCKWISE);
    private final Piece green2 = onBoard(Colour.GREEN, 2, Direction.CLOCKWISE);
    private final Piece green3 = onBoard(Colour.GREEN, 3, Direction.CLOCKWISE);
    private final Piece red3 = new Piece(Colour.RED, 3);
    private final Piece green4 = new Piece(Colour.GREEN, 4);
    private final Piece yellow1 = onBoard(Colour.YELLOW, 1, Direction.CLOCKWISE);
    private final Piece yellow2 = onBoard(Colour.YELLOW, 2, Direction.CLOCKWISE);
    private final Piece yellow3 = new Piece(Colour.YELLOW, 3);
    private final Piece blue1 = onBoard(Colour.BLUE, 1, Direction.CLOCKWISE);
    private final Piece blue2 = onBoard(Colour.BLUE, 2, Direction.CLOCKWISE);
    private final Piece blue3 = onBoard(Colour.BLUE, 3, Direction.COUNTER_CLOCKWISE);

    // R11: four pieces R1 to R4 start in base; the player finishes when all four are home
    @Test
    void playerFinishesWhenAllFourPiecesAreHome() {
        assertEquals(List.of("R1", "R2", "R3", "R4"), red.pieces().stream().map(Piece::name).toList());
        assertEquals(4, red.piecesInBase());
        assertFalse(red.hasFinished());
        red.pieces().forEach(piece -> piece.moveTo(Position.home()));
        assertTrue(red.hasFinished());
    }

    // DTO: the status is a snapshot with every piece's location
    @Test
    void statusDescribesEveryPiece() {
        red.pieces().get(0).enterBoard(Direction.CLOCKWISE);
        PlayerStatusDto status = red.status();
        assertEquals(1, status.piecesOnBoard());
        assertEquals(3, status.piecesInBase());
        assertEquals(4, status.pieces().size());
        assertEquals(new PlayerStatusDto.PieceLocation("R1", "26"), status.pieces().get(0));
    }

    // Red: captures first, choosing the victim closest to its home
    @Test
    void redCapturesTheVictimClosestToItsHome() {
        green2.moveTo(Position.onTrack(35));
        MoveOption nearVictim = capture(red2, green2);
        List<MoveOption> options = List.of(enter(red3), capture(red1, green1), nearVictim);
        assertSame(nearVictim, red.chooseMove(options));
    }

    // Red: with a six and no capture, brings a piece out of base
    @Test
    void redEntersOnSixWhenNoCaptureIsPossible() {
        MoveOption entry = enter(red3);
        assertSame(entry, red.chooseMove(List.of(move(red1), entry)));
    }

    // Red: avoids forming a block when another move exists
    @Test
    void redAvoidsFormingABlock() {
        MoveOption plainMove = move(red2);
        assertSame(plainMove, red.chooseMove(List.of(formingBlock(red1), plainMove)));
    }

    // Green: forming a block comes first, even before leaving base
    @Test
    void greenPrefersFormingABlockToEntering() {
        MoveOption blockMaker = formingBlock(green1);
        assertSame(blockMaker, green.chooseMove(List.of(enter(green4), blockMaker)));
    }

    // Green: brings a piece out on a six before making a plain move
    @Test
    void greenEntersOnSix() {
        MoveOption entry = enter(green4);
        assertSame(entry, green.chooseMove(List.of(move(green3), entry)));
    }

    // Green + T-4: moves other pieces, then the whole block, and breaks a block last
    @Test
    void greenBreaksItsBlockOnlyAsALastResort() {
        MoveOption otherPiece = move(green3);
        MoveOption moveTogether = blockMove(green1, green2);
        MoveOption breakAway = leavingBlock(green1);
        assertSame(otherPiece, green.chooseMove(List.of(breakAway, moveTogether, otherPiece)));
        assertSame(moveTogether, green.chooseMove(List.of(breakAway, moveTogether)));
    }

    // Yellow: a six always brings a piece out, keeping the base empty
    @Test
    void yellowAlwaysEntersOnSix() {
        MoveOption entry = enter(yellow3);
        assertSame(entry, yellow.chooseMove(List.of(capture(yellow1, red1), entry)));
    }

    // Yellow: captures with a piece that still needs a capture
    @Test
    void yellowCapturesWithAPieceThatNeedsOne() {
        MoveOption capture = capture(yellow1, red1);
        assertSame(capture, yellow.chooseMove(List.of(move(yellow2), capture)));
    }

    // Yellow: ignores unneeded captures and moves the piece closest to home
    @Test
    void yellowMovesThePieceClosestToHome() {
        yellow1.recordCapture();
        yellow2.moveTo(Position.inHomeStraight(1));
        MoveOption closestToHome = move(yellow2);
        assertSame(closestToHome, yellow.chooseMove(List.of(capture(yellow1, red1), closestToHome)));
    }

    // Blue: moves its pieces in a cycle, B1 then B2
    @Test
    void blueMovesItsPiecesInACycle() {
        MoveOption first = move(blue1);
        MoveOption second = move(blue2);
        assertSame(first, blue.chooseMove(List.of(second, first)));
        assertSame(second, blue.chooseMove(List.of(move(blue1), second)));
    }

    // Blue + A4: a piece that cannot move is skipped, and the cycle wraps round
    @Test
    void blueSkipsAPieceWithNoMoves() {
        blue.chooseMove(List.of(move(blue1)));
        MoveOption third = move(blue3);
        assertSame(third, blue.chooseMove(List.of(move(blue1), third)));
        MoveOption wrapped = move(blue1);
        assertSame(wrapped, blue.chooseMove(List.of(move(blue2), wrapped)));
    }

    // Blue: avoids the mystery cell clockwise and seeks it counter-clockwise
    @Test
    void blueMysteryCellPreferenceDependsOnDirection() {
        MoveOption safe = move(blue1);
        assertSame(safe, blue.chooseMove(List.of(ontoMystery(blue1), safe)));
        blue.chooseMove(List.of(move(blue2)));
        MoveOption mystery = ontoMystery(blue3);
        assertSame(mystery, blue.chooseMove(List.of(move(blue3), mystery)));
    }

    // A5: a piece in the home straight still counts as on the board
    @Test
    void homeStraightPieceCountsAsOnTheBoard() {
        red.pieces().get(0).enterBoard(Direction.CLOCKWISE);
        red.pieces().get(0).moveTo(Position.inHomeStraight(2));
        assertEquals(1, red.piecesOnBoard());
        assertEquals(3, red.piecesInBase());
    }

    // Green: never forms a new block by breaking up one it already has
    @Test
    void greenDoesNotBreakABlockToFormAnother() {
        MoveOption otherPiece = move(green3);
        assertSame(otherPiece, green.chooseMove(List.of(movingBlockToBlock(green1), otherPiece)));
    }

    private static Piece onBoard(Colour colour, int number, Direction direction) {
        Piece piece = new Piece(colour, number);
        piece.enterBoard(direction);
        return piece;
    }

    private static MoveOption enter(Piece piece) {
        Route route = Route.completed(Position.base(), Position.onTrack(piece.colour().startCell()), 0, 0);
        return new MoveOption(Type.ENTER_BOARD, List.of(piece), route, Landing.offTrack(), false);
    }

    private static MoveOption move(Piece piece) {
        return new MoveOption(Type.MOVE_PIECE, List.of(piece), stay(piece), Landing.offTrack(), false);
    }

    private static MoveOption capture(Piece piece, Piece victim) {
        Landing landing = new Landing(List.of(victim), false, false);
        return new MoveOption(Type.MOVE_PIECE, List.of(piece), stay(piece), landing, false);
    }

    private static MoveOption formingBlock(Piece piece) {
        Landing landing = new Landing(List.of(), true, false);
        return new MoveOption(Type.MOVE_PIECE, List.of(piece), stay(piece), landing, false);
    }

    private static MoveOption movingBlockToBlock(Piece piece) {
        Landing landing = new Landing(List.of(), true, false);
        return new MoveOption(Type.MOVE_PIECE, List.of(piece), stay(piece), landing, true);
    }

    private static MoveOption leavingBlock(Piece piece) {
        return new MoveOption(Type.MOVE_PIECE, List.of(piece), stay(piece), Landing.offTrack(), true);
    }

    private static MoveOption blockMove(Piece first, Piece second) {
        return new MoveOption(Type.MOVE_BLOCK, List.of(first, second), stay(first), Landing.offTrack(), false);
    }

    private static MoveOption ontoMystery(Piece piece) {
        Landing landing = new Landing(List.of(), false, true);
        return new MoveOption(Type.MOVE_PIECE, List.of(piece), stay(piece), landing, false);
    }

    private static Route stay(Piece piece) {
        return Route.completed(piece.position(), piece.position(), 1, 0);
    }
}
