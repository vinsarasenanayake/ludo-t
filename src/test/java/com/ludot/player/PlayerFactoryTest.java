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

    @Test
    void redCapturesTheVictimClosestToItsHome() {
        board.move(green2, Position.onTrack(35));
        MoveOption nearVictim = capture(red2, green2);
        List<MoveOption> options = List.of(enter(red3), capture(red1, green1), nearVictim);
        assertSame(nearVictim, red.chooseMove(options));
    }

    @Test
    void redEntersOnSixWhenNoCaptureIsPossible() {
        MoveOption entry = enter(red3);
        assertSame(entry, red.chooseMove(List.of(move(red1), entry)));
    }

    @Test
    void redAvoidsFormingABlock() {
        MoveOption plainMove = move(red2);
        assertSame(plainMove, red.chooseMove(List.of(formingBlock(red1), plainMove)));
    }

    @Test
    void redDoesNotEnterOntoItsOwnPieceWhenAnotherMoveExists() {
        MoveOption otherMove = move(red1);
        assertSame(otherMove, red.chooseMove(List.of(enterFormingBlock(red3), otherMove)));
    }

    @Test
    void redEntersOntoItsOwnPieceWhenEveryMoveFormsABlock() {
        MoveOption entry = enterFormingBlock(red3);
        assertSame(entry, red.chooseMove(List.of(entry, formingBlock(red1))));
    }

    @Test
    void greenPrefersFormingABlockToEntering() {
        MoveOption blockMaker = formingBlock(green1);
        assertSame(blockMaker, green.chooseMove(List.of(enter(green4), blockMaker)));
    }

    @Test
    void greenDoesNotBreakABlockToFormAnother() {
        MoveOption otherPiece = move(green3);
        assertSame(otherPiece, green.chooseMove(List.of(movingBlockToBlock(green1), otherPiece)));
    }

    @Test
    void greenEntersOnSix() {
        MoveOption entry = enter(green4);
        assertSame(entry, green.chooseMove(List.of(move(green3), entry)));
    }

    @Test
    void greenPrefersTheBlockMoveToMovingAnotherPiece() {
        MoveOption moveTogether = blockMove(green1, green2);
        List<MoveOption> options = List.of(leavingBlock(green1), move(green3), moveTogether);
        assertSame(moveTogether, green.chooseMove(options));
    }

    @Test
    void greenMovesAnotherPieceBeforeBreakingItsBlock() {
        MoveOption otherPiece = move(green3);
        assertSame(otherPiece, green.chooseMove(List.of(leavingBlock(green1), otherPiece)));
    }

    @Test
    void greenCapturesWithAPieceThatNeedsOne() {
        MoveOption capture = capture(green3, red1);
        assertSame(capture, green.chooseMove(List.of(move(green1), capture)));
    }

    @Test
    void greenIgnoresACaptureItDoesNotNeed() {
        green3.recordCapture();
        board.move(green1, Position.inHomeStraight(1));
        MoveOption closestToHome = move(green1);
        assertSame(closestToHome, green.chooseMove(List.of(capture(green3, red1), closestToHome)));
    }

    @Test
    void yellowAlwaysEntersOnSix() {
        MoveOption entry = enter(yellow3);
        assertSame(entry, yellow.chooseMove(List.of(capture(yellow1, red1), entry)));
    }

    @Test
    void yellowCapturesWithAPieceThatNeedsOne() {
        MoveOption capture = capture(yellow1, red1);
        assertSame(capture, yellow.chooseMove(List.of(move(yellow2), capture)));
    }

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