package com.ludot.player;

import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.rules.MoveOption;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.List;

import static com.ludot.testsupport.TestDoubles.blockMove;
import static com.ludot.testsupport.TestDoubles.capture;
import static com.ludot.testsupport.TestDoubles.enter;
import static com.ludot.testsupport.TestDoubles.formingBlock;
import static com.ludot.testsupport.TestDoubles.leavingBlock;
import static com.ludot.testsupport.TestDoubles.move;
import static com.ludot.testsupport.TestDoubles.ontoMystery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    private final PlayerFactory factory = new PlayerFactory(new TrackNavigator());

    @Nested
    class PlayerBasics {

        private final Player red = factory.createPlayer(Colour.RED);

        @Test
        @DisplayName("Rule 11: four pieces R1-R4 start in base; the player finishes when all four are home")
        void playerFinishesWhenAllFourPiecesAreHome() {
            assertEquals(List.of("R1", "R2", "R3", "R4"), red.pieces().stream().map(Piece::name).toList());
            assertEquals(4, red.piecesInBase());
            assertFalse(red.hasFinished());
            red.pieces().forEach(piece -> piece.moveTo(Position.home()));
            assertTrue(red.hasFinished());
        }

        @Test
        @DisplayName("DTO: the status is a read-only snapshot with every piece's location")
        void statusDescribesEveryPiece() {
            red.pieces().get(0).enterBoard(Direction.CLOCKWISE);
            PlayerStatusDto status = red.status();
            assertEquals(1, status.piecesOnBoard());
            assertEquals(3, status.piecesInBase());
            assertEquals(new PlayerStatusDto.PieceLocation("R1", "26"), status.pieces().get(0));
        }
    }

    @Nested
    class RedAggressiveCapture {

        private final Player red = factory.createPlayer(Colour.RED);
        private final Piece red1 = onBoard(Colour.RED, 1);
        private final Piece red2 = onBoard(Colour.RED, 2);
        private final Piece green1 = onBoard(Colour.GREEN, 1);
        private final Piece green2 = new Piece(Colour.GREEN, 2);

        @Test
        @DisplayName("Red captures first, choosing the victim closest to its home")
        void capturesTheVictimClosestToItsHome() {
            green2.moveTo(Position.inHomeStraight(3));
            MoveOption nearVictim = capture(red2, green2);
            List<MoveOption> options = List.of(enter(new Piece(Colour.RED, 3)), capture(red1, green1), nearVictim);
            assertSame(nearVictim, red.chooseMove(options));
        }

        @Test
        @DisplayName("With a six and no capture, Red brings a piece out of base")
        void entersOnSixWhenNoCaptureIsPossible() {
            MoveOption entry = enter(new Piece(Colour.RED, 3));
            assertSame(entry, red.chooseMove(List.of(move(red1), entry)));
        }

        @Test
        @DisplayName("Red avoids forming a block when another move exists")
        void avoidsFormingABlock() {
            MoveOption plainMove = move(red2);
            assertSame(plainMove, red.chooseMove(List.of(formingBlock(red1), plainMove)));
        }
    }

    @Nested
    class GreenBlocking {

        private final Player green = factory.createPlayer(Colour.GREEN);
        private final Piece green1 = onBoard(Colour.GREEN, 1);
        private final Piece green2 = onBoard(Colour.GREEN, 2);
        private final Piece green3 = onBoard(Colour.GREEN, 3);
        private final Piece green4 = new Piece(Colour.GREEN, 4);

        @Test
        @DisplayName("Green's first priority is forming a block, even over leaving base")
        void formingABlockBeatsEntering() {
            MoveOption blockMaker = formingBlock(green1);
            assertSame(blockMaker, green.chooseMove(List.of(enter(green4), blockMaker)));
        }

        @Test
        @DisplayName("Green brings a piece out on a six before making a plain move")
        void entersOnSix() {
            MoveOption entry = enter(green4);
            assertSame(entry, green.chooseMove(List.of(move(green3), entry)));
        }

        @Test
        @DisplayName("Rule T-4: Green moves other pieces, then the whole block, and breaks a block last")
        void breaksTheBlockOnlyAsALastResort() {
            MoveOption otherPiece = move(green3);
            MoveOption moveTogether = blockMove(green1, green2);
            MoveOption breakAway = leavingBlock(green1);
            assertSame(otherPiece, green.chooseMove(List.of(breakAway, moveTogether, otherPiece)));
            assertSame(moveTogether, green.chooseMove(List.of(breakAway, moveTogether)));
        }
    }

    @Nested
    class YellowWinning {

        private final Player yellow = factory.createPlayer(Colour.YELLOW);
        private final Piece yellow1 = onBoard(Colour.YELLOW, 1);
        private final Piece yellow2 = onBoard(Colour.YELLOW, 2);
        private final Piece red1 = new Piece(Colour.RED, 1);

        @Test
        @DisplayName("Yellow keeps an empty base: a six always brings a piece out")
        void alwaysEntersOnSix() {
            MoveOption entry = enter(new Piece(Colour.YELLOW, 3));
            assertSame(entry, yellow.chooseMove(List.of(capture(yellow1, red1), entry)));
        }

        @Test
        @DisplayName("Yellow captures with a piece that still needs a capture")
        void capturesWithAPieceThatNeedsOne() {
            MoveOption capture = capture(yellow1, red1);
            assertSame(capture, yellow.chooseMove(List.of(move(yellow2), capture)));
        }

        @Test
        @DisplayName("Otherwise Yellow ignores unneeded captures and moves the piece closest to home")
        void movesThePieceClosestToHome() {
            yellow1.recordCapture();
            yellow2.moveTo(Position.inHomeStraight(1));
            MoveOption closestToHome = move(yellow2);
            assertSame(closestToHome, yellow.chooseMove(List.of(capture(yellow1, red1), closestToHome)));
        }
    }

    @Nested
    class BlueCyclicMystery {

        private final PlayerStrategy blue = new CyclicMysteryStrategy();
        private final Piece blue1 = onBoard(Colour.BLUE, 1);
        private final Piece blue2 = onBoard(Colour.BLUE, 2);
        private final Piece blue3 = new Piece(Colour.BLUE, 3);

        BlueCyclicMystery() {
            blue3.enterBoard(Direction.COUNTER_CLOCKWISE);
        }

        @Test
        @DisplayName("Blue moves its pieces in a cycle: B1, then B2")
        void movesPiecesInACycle() {
            Player bluePlayer = factory.createPlayer(Colour.BLUE);
            MoveOption first = move(blue1);
            MoveOption second = move(blue2);
            assertSame(first, bluePlayer.chooseMove(List.of(second, first)));
            assertSame(second, bluePlayer.chooseMove(List.of(move(blue1), second)));
        }

        @Test
        @DisplayName("Assumption A16: a piece that cannot move is skipped, and the cycle wraps round")
        void skipsAPieceWithNoMoves() {
            blue.chooseMove(List.of(move(blue1)));
            MoveOption third = move(blue3);
            assertSame(third, blue.chooseMove(List.of(move(blue1), third)));
            MoveOption wrapped = move(blue1);
            assertSame(wrapped, blue.chooseMove(List.of(move(blue2), wrapped)));
        }

        @Test
        @DisplayName("Blue avoids the mystery cell clockwise and seeks it counter-clockwise")
        void mysteryCellPreferenceDependsOnDirection() {
            MoveOption safe = move(blue1);
            assertSame(safe, blue.chooseMove(List.of(ontoMystery(blue1), safe)));
            blue.chooseMove(List.of(move(blue2)));
            MoveOption mystery = ontoMystery(blue3);
            assertSame(mystery, blue.chooseMove(List.of(move(blue3), mystery)));
        }
    }

    private static Piece onBoard(Colour colour, int number) {
        Piece piece = new Piece(colour, number);
        piece.enterBoard(Direction.CLOCKWISE);
        return piece;
    }
}
