package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.player.MoveRule.AvoidBlockClosestToHome;
import com.ludot.player.MoveRule.BlockMove;
import com.ludot.player.MoveRule.CaptureByPieceNeedingCapture;
import com.ludot.player.MoveRule.CaptureClosestToVictimHome;
import com.ludot.player.MoveRule.ClosestToHome;
import com.ludot.player.MoveRule.EnterFromBase;
import com.ludot.player.MoveRule.FirstAvailable;
import com.ludot.player.MoveRule.FormBlock;
import com.ludot.player.MoveRule.NonBlockPieceClosestToHome;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.testsupport.TestDoubles.blockMove;
import static com.ludot.testsupport.TestDoubles.capture;
import static com.ludot.testsupport.TestDoubles.enter;
import static com.ludot.testsupport.TestDoubles.formingBlock;
import static com.ludot.testsupport.TestDoubles.leavingBlock;
import static com.ludot.testsupport.TestDoubles.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class MoveRuleTest {

    private static final MoveOption PASSED_ON = move(new Piece(Colour.BLUE, 4));
    private static final PlayerStrategy NEXT = options -> PASSED_ON;

    private final TrackNavigator navigator = new TrackNavigator();
    private final Piece red1 = new Piece(Colour.RED, 1);
    private final Piece red2 = new Piece(Colour.RED, 2);
    private final Piece green1 = new Piece(Colour.GREEN, 1);
    private final Piece green2 = new Piece(Colour.GREEN, 2);
    private final Piece green3 = new Piece(Colour.GREEN, 3);
    private final Piece yellow1 = new Piece(Colour.YELLOW, 1);

    @Test
    void firstAvailableAlwaysReturnsTheFirstOption() {
        MoveOption first = move(new Piece(Colour.RED, 1));
        MoveOption second = move(new Piece(Colour.RED, 2));
        assertSame(first, new FirstAvailable().chooseMove(List.of(first, second)));
    }

    @Test
    void avoidBlockClosestToHomePicksAMoveThatDoesNotFormABlock() {
        red1.enterBoard(Direction.CLOCKWISE);
        red2.enterBoard(Direction.CLOCKWISE);
        MoveOption plain = move(red2);
        assertSame(plain, new AvoidBlockClosestToHome(navigator, NEXT).chooseMove(List.of(formingBlock(red1), plain)));
    }

    @Test
    void avoidBlockClosestToHomePassesOnWhenEveryMoveFormsABlock() {
        assertSame(PASSED_ON, new AvoidBlockClosestToHome(navigator, NEXT).chooseMove(List.of(formingBlock(red1))));
    }

    @Test
    void blockMovePicksTheBlockMove() {
        MoveOption together = blockMove(green1, green2);
        assertSame(together, new BlockMove(NEXT).chooseMove(List.of(move(green1), together)));
    }

    @Test
    void blockMovePassesOnWhenThereIsNoBlock() {
        assertSame(PASSED_ON, new BlockMove(NEXT).chooseMove(List.of(move(green1))));
    }

    @Test
    void captureByPieceNeedingCapturePicksACaptureByAPieceWithNoCapturesYet() {
        MoveOption capture = capture(yellow1, red1);
        assertSame(capture, new CaptureByPieceNeedingCapture(NEXT).chooseMove(List.of(capture)));
    }

    @Test
    void captureByPieceNeedingCapturePassesOnWhenThePieceAlreadyCaptured() {
        yellow1.recordCapture();
        assertSame(PASSED_ON, new CaptureByPieceNeedingCapture(NEXT).chooseMove(List.of(capture(yellow1, red1))));
    }

    @Test
    void captureClosestToVictimHomePicksTheVictimNearestItsHome() {
        green1.enterBoard(Direction.CLOCKWISE);
        green2.moveTo(Position.inHomeStraight(4));
        MoveOption nearHome = capture(red2, green2);
        assertSame(nearHome, new CaptureClosestToVictimHome(navigator, NEXT).chooseMove(List.of(capture(red1, green1), nearHome)));
    }

    @Test
    void captureClosestToVictimHomePassesOnWhenNothingCanBeCaptured() {
        assertSame(PASSED_ON, new CaptureClosestToVictimHome(navigator, NEXT).chooseMove(List.of(move(red1))));
    }

    @Test
    void closestToHomePicksThePieceClosestToHome() {
        Piece yellow2 = new Piece(Colour.YELLOW, 2);
        yellow1.enterBoard(Direction.CLOCKWISE);
        yellow2.moveTo(Position.inHomeStraight(1));
        MoveOption nearHome = move(yellow2);
        assertSame(nearHome, new ClosestToHome(navigator, NEXT).chooseMove(List.of(move(yellow1), nearHome)));
    }

    @Test
    void enterFromBasePicksTheEntryOption() {
        MoveOption entry = enter(red1);
        assertSame(entry, new EnterFromBase(NEXT).chooseMove(List.of(move(red1), entry)));
    }

    @Test
    void enterFromBasePassesOnWhenThereIsNoEntry() {
        assertSame(PASSED_ON, new EnterFromBase(NEXT).chooseMove(List.of(move(red1))));
    }

    @Test
    void formBlockPicksTheOptionThatFormsABlock() {
        MoveOption blockMaker = formingBlock(green1);
        assertSame(blockMaker, new FormBlock(NEXT).chooseMove(List.of(move(green1), blockMaker)));
    }

    @Test
    void formBlockPassesOnWhenNoOptionFormsABlock() {
        assertSame(PASSED_ON, new FormBlock(NEXT).chooseMove(List.of(move(green1))));
    }

    @Test
    void nonBlockPieceClosestToHomePicksAPieceThatIsNotInABlock() {
        green1.enterBoard(Direction.CLOCKWISE);
        green3.enterBoard(Direction.CLOCKWISE);
        MoveOption free = move(green3);
        assertSame(free, new NonBlockPieceClosestToHome(navigator, NEXT).chooseMove(List.of(leavingBlock(green1), free)));
    }

    @Test
    void nonBlockPieceClosestToHomePassesOnWhenOnlyBlockPiecesCanMove() {
        assertSame(PASSED_ON, new NonBlockPieceClosestToHome(navigator, NEXT).chooseMove(List.of(leavingBlock(green1))));
    }
}
