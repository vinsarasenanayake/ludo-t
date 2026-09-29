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
import static com.ludot.testsupport.MoveOptions.move;
import static org.junit.jupiter.api.Assertions.assertSame;

class WinningStrategyTest {

    private WinningStrategy yellow;
    private Piece yellow1;
    private Piece yellow2;
    private Piece yellow3;
    private Piece red1;

    @BeforeEach
    void setUp() {
        yellow = new WinningStrategy(new TrackNavigator());
        yellow1 = new Piece(Colour.YELLOW, 1);
        yellow2 = new Piece(Colour.YELLOW, 2);
        yellow3 = new Piece(Colour.YELLOW, 3);
        red1 = new Piece(Colour.RED, 1);
        yellow1.enterBoard(Direction.CLOCKWISE);
        yellow2.enterBoard(Direction.CLOCKWISE);
    }

    @Test
    @DisplayName("Yellow keeps an empty base: a six always brings a piece out")
    void alwaysEntersOnSix() {
        MoveOption entry = enter(yellow3);
        assertSame(entry, yellow.chooseMove(context(6, capture(yellow1, red1), entry)));
    }

    @Test
    @DisplayName("Yellow captures with a piece that still needs a capture")
    void capturesWithAPieceThatNeedsOne() {
        MoveOption capture = capture(yellow1, red1);
        assertSame(capture, yellow.chooseMove(context(3, move(yellow2), capture)));
    }

    @Test
    @DisplayName("Yellow does not chase captures it no longer needs")
    void ignoresCaptureByAPieceThatAlreadyCaptured() {
        yellow1.recordCapture();
        yellow2.moveTo(Position.inHomeStraight(1));
        MoveOption closestToHome = move(yellow2);
        assertSame(closestToHome, yellow.chooseMove(context(3, capture(yellow1, red1), closestToHome)));
    }

    @Test
    @DisplayName("Without captures, Yellow moves the piece closest to home")
    void movesThePieceClosestToHome() {
        yellow2.moveTo(Position.inHomeStraight(2));
        MoveOption closestToHome = move(yellow2);
        assertSame(closestToHome, yellow.chooseMove(context(3, move(yellow1), closestToHome)));
    }
}