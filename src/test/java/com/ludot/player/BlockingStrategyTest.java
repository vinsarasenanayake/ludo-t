package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ludot.testsupport.MoveOptions.blockMove;
import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.enter;
import static com.ludot.testsupport.MoveOptions.formingBlock;
import static com.ludot.testsupport.MoveOptions.leavingBlock;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class BlockingStrategyTest {

    private BlockingStrategy green;
    private Piece green1;
    private Piece green2;
    private Piece green3;
    private Piece green4;

    @BeforeEach
    void setUp() {
        green = new BlockingStrategy(new TrackNavigator());
        green1 = new Piece(Colour.GREEN, 1);
        green2 = new Piece(Colour.GREEN, 2);
        green3 = new Piece(Colour.GREEN, 3);
        green4 = new Piece(Colour.GREEN, 4);
        green1.enterBoard(Direction.CLOCKWISE);
        green2.enterBoard(Direction.CLOCKWISE);
        green3.enterBoard(Direction.CLOCKWISE);
    }

    @Test
    @DisplayName("Green moving six to create a block beats leaving base")
    void formingABlockBeatsEntering() {
        MoveOption blockMaker = formingBlock(green1);
        assertSame(blockMaker, green.chooseMove(context(6, enter(green4), blockMaker)));
    }

    @Test
    @DisplayName("Green keeps an empty base: a six brings a piece out")
    void entersOnSix() {
        MoveOption entry = enter(green4);
        assertSame(entry, green.chooseMove(context(6, move(green1), entry)));
    }

    @Test
    @DisplayName("Green moves its other pieces before breaking a block")
    void movesOtherPiecesBeforeBreakingABlock() {
        MoveOption otherPiece = move(green3);
        assertSame(otherPiece, green.chooseMove(context(4, leavingBlock(green1), otherPiece)));
    }

    @Test
    @DisplayName("Rule T-4: Green uses the block move before breaking the block")
    void prefersBlockMoveToBreakingTheBlock() {
        MoveOption moveTogether = blockMove(green1, green2);
        assertSame(moveTogether, green.chooseMove(context(4, leavingBlock(green1), moveTogether)));
    }

    @Test
    @DisplayName("Green breaks a block only when nothing else can use the roll")
    void breaksTheBlockAsLastResort() {
        MoveOption breakAway = leavingBlock(green1);
        assertSame(breakAway, green.chooseMove(context(1, breakAway)));
    }
}