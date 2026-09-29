package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ludot.testsupport.MoveOptions.capture;
import static com.ludot.testsupport.MoveOptions.context;
import static com.ludot.testsupport.MoveOptions.enter;
import static com.ludot.testsupport.MoveOptions.formingBlock;
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class AggressiveCaptureStrategyTest {

    private AggressiveCaptureStrategy red;
    private Piece red1;
    private Piece red2;
    private Piece green1;
    private Piece green2;

    @BeforeEach
    void setUp() {
        red = new AggressiveCaptureStrategy(new TrackNavigator());
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
        green1 = new Piece(Colour.GREEN, 1);
        green2 = new Piece(Colour.GREEN, 2);
        red1.enterBoard(Direction.CLOCKWISE);
        red2.enterBoard(Direction.CLOCKWISE);
    }

    @Test
    @DisplayName("Red prefers capturing to any other move")
    void capturesBeforeAnythingElse() {
        MoveOption capture = capture(red1, green1);
        assertSame(capture, red.chooseMove(context(move(red2), capture)));
    }

    @Test
    @DisplayName("Red captures the opponent piece closest to its home")
    void capturesTheVictimClosestToItsHome() {
        green1.enterBoard(Direction.CLOCKWISE);
        green2.moveTo(Position.inHomeStraight(3));
        MoveOption farVictim = capture(red1, green1);
        MoveOption nearVictim = capture(red2, green2);
        assertSame(nearVictim, red.chooseMove(context(farVictim, nearVictim)));
    }

    @Test
    @DisplayName("With a six and no capture, Red brings a piece out of base")
    void entersOnSixWhenNoCaptureIsPossible() {
        Piece red3 = new Piece(Colour.RED, 3);
        MoveOption entry = enter(red3);
        assertSame(entry, red.chooseMove(context(move(red1), entry)));
    }

    @Test
    @DisplayName("Red avoids forming a block when another move exists")
    void avoidsFormingABlock() {
        MoveOption plainMove = move(red2);
        assertSame(plainMove, red.chooseMove(context(formingBlock(red1), plainMove)));
    }

    @Test
    @DisplayName("Red forms a block only when it is unavoidable")
    void formsABlockWhenItIsTheOnlyMove() {
        MoveOption onlyMove = formingBlock(red1);
        assertSame(onlyMove, red.chooseMove(context(onlyMove)));
    }
}
