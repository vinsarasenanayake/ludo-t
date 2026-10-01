package com.ludot.player;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.rules.MoveOption;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.player.MoveOptions.blockMove;
import static com.ludot.player.MoveOptions.capture;
import static com.ludot.player.MoveOptions.enter;
import static com.ludot.player.MoveOptions.enterFormingBlock;
import static com.ludot.player.MoveOptions.formingBlock;
import static com.ludot.player.MoveOptions.leavingBlock;
import static com.ludot.player.MoveOptions.move;
import static com.ludot.player.MoveOptions.movingBlockToBlock;
import static org.junit.jupiter.api.Assertions.assertSame;

class PlayerFactoryTest {

    private final Board board = new Board();
    private final PlayerFactory factory = new PlayerFactory(new TrackNavigator());
    private final Player red = factory.createPlayer(Colour.RED);
    private final Player green = factory.createPlayer(Colour.GREEN);
    private final Player yellow = factory.createPlayer(Colour.YELLOW);

    private final Piece red1 = onBoard(Colour.RED, 1);
    private final Piece red2 = onBoard(Colour.RED, 2);
    private final Piece red3 = new Piece(Colour.RED, 3);
    private final Piece green1 = onBoard(Colour.GREEN, 1);
    private final Piece green2 = onBoard(Colour.GREEN, 2);
    private final Piece green3 = onBoard(Colour.GREEN, 3);
    private final Piece green4 = new Piece(Colour.GREEN, 4);
    private final Piece yellow1 = onBoard(Colour.YELLOW, 1);
    private final Piece yellow2 = onBoard(Colour.YELLOW, 2);
    private final Piece yellow3 = new Piece(Colour.YELLOW, 3);

    // Red (2.1.1): captures first, choosing the victim closest to its home
    @Test
    void redCapturesTheVictimClosestToItsHome() {
        board.move(green2, Position.onTrack(35));
        MoveOption nearVictim = capture(red2, green2);
        List<MoveOption> options = List.of(enter(red3), capture(red1, green1), nearVictim);
        assertSame(nearVictim, red.chooseMove(options));
    }

    // Red (2.1.1): with a six and no capture, brings a piece out of base
    @Test
    void redEntersOnSixWhenNoCaptureIsPossible() {
        MoveOption entry = enter(red3);
        assertSame(entry, red.chooseMove(List.of(move(red1), entry)));
    }

    // Red (2.1.1): avoids forming a block when another move exists
    @Test
    void redAvoidsFormingABlock() {
        MoveOption plainMove = move(red2);
        assertSame(plainMove, red.chooseMove(List.of(formingBlock(red1), plainMove)));
    }

    // Red (2.1.1): with no capture, a six must bring a piece to X, so a block formed there is unavoidable
    @Test
    void redEntersOnSixEvenWhenThatFormsABlock() {
        MoveOption entry = enterFormingBlock(red3);
        assertSame(entry, red.chooseMove(List.of(entry, move(red1))));
    }

    // Red (2.1.1): forms a block when it is unavoidable
    @Test
    void redFormsABlockWhenItIsTheOnlyMove() {
        MoveOption onlyMove = formingBlock(red1);
        assertSame(onlyMove, red.chooseMove(List.of(onlyMove)));
    }

    // Green (2.1.2): forming a block comes first, even before leaving base
    @Test
    void greenPrefersFormingABlockToEntering() {
        MoveOption blockMaker = formingBlock(green1);
        assertSame(blockMaker, green.chooseMove(List.of(enter(green4), blockMaker)));
    }

    // Green (2.1.2): never forms a new block by breaking up one it already has
    @Test
    void greenDoesNotBreakABlockToFormAnother() {
        MoveOption otherPiece = move(green3);
        assertSame(otherPiece, green.chooseMove(List.of(movingBlockToBlock(green1), otherPiece)));
    }

    // Green (2.1.2): brings a piece out on a six before making a plain move
    @Test
    void greenEntersOnSix() {
        MoveOption entry = enter(green4);
        assertSame(entry, green.chooseMove(List.of(move(green3), entry)));
    }

    // Green (2.1.2): moves a piece outside the block before touching the block
    @Test
    void greenMovesAnotherPieceBeforeItsBlock() {
        MoveOption otherPiece = move(green3);
        List<MoveOption> options = List.of(leavingBlock(green1), blockMove(green1, green2), otherPiece);
        assertSame(otherPiece, green.chooseMove(options));
    }

    // Green (2.1.2) + T-4: moves the whole block together rather than breaking it
    @Test
    void greenMovesTheBlockTogetherBeforeBreakingIt() {
        MoveOption moveTogether = blockMove(green1, green2);
        assertSame(moveTogether, green.chooseMove(List.of(leavingBlock(green1), moveTogether)));
    }

    // Green (2.1.2): breaks its block only when nothing else can use the roll
    @Test
    void greenBreaksItsBlockOnlyAsALastResort() {
        MoveOption breakAway = leavingBlock(green1);
        assertSame(breakAway, green.chooseMove(List.of(breakAway)));
    }

    // Yellow (2.1.3): a six always brings a piece out, keeping the base empty
    @Test
    void yellowAlwaysEntersOnSix() {
        MoveOption entry = enter(yellow3);
        assertSame(entry, yellow.chooseMove(List.of(capture(yellow1, red1), entry)));
    }

    // Yellow (2.1.3): captures with a piece that still needs a capture
    @Test
    void yellowCapturesWithAPieceThatNeedsOne() {
        MoveOption capture = capture(yellow1, red1);
        assertSame(capture, yellow.chooseMove(List.of(move(yellow2), capture)));
    }

    // Yellow (2.1.3): ignores unneeded captures and moves the piece closest to home
    @Test
    void yellowMovesThePieceClosestToHome() {
        yellow1.recordCapture();
        board.move(yellow2, Position.inHomeStraight(1));
        MoveOption closestToHome = move(yellow2);
        assertSame(closestToHome, yellow.chooseMove(List.of(capture(yellow1, red1), closestToHome)));
    }

    private Piece onBoard(Colour colour, int number) {
        Piece piece = new Piece(colour, number);
        board.enter(piece, Direction.CLOCKWISE);
        return piece;
    }
}
