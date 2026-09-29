package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;
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

        private final Player red = new Player(Colour.RED, options -> options.get(0));

        @Test
        @DisplayName("Each player has four pieces named 1 to 4")
        void playerHasFourPiecesNamedOneToFour() {
            List<String> names = red.pieces().stream().map(Piece::name).toList();
            assertEquals(List.of("R1", "R2", "R3", "R4"), names);
        }

        @Test
        void allPiecesStartInBase() {
            assertEquals(4, red.piecesInBase());
            assertEquals(0, red.piecesOnBoard());
        }

        @Test
        void pieceOnTheTrackCountsAsOnTheBoard() {
            red.pieces().get(0).enterBoard(Direction.CLOCKWISE);
            assertEquals(1, red.piecesOnBoard());
            assertTrue(red.hasPieceOnTrack());
        }

        @Test
        void playerHasNotFinishedWhilePiecesAreOut() {
            red.pieces().get(0).moveTo(Position.home());
            assertFalse(red.hasFinished());
        }

        @Test
        @DisplayName("Rule 11: a player finishes when all four pieces are home")
        void playerFinishesWhenAllPiecesAreHome() {
            red.pieces().forEach(piece -> piece.moveTo(Position.home()));
            assertTrue(red.hasFinished());
        }

        @Test
        @DisplayName("Strategy: the player delegates the decision to its strategy")
        void chooseMoveDelegatesToTheStrategy() {
            MoveOption option = move(red.pieces().get(0));
            assertSame(option, red.chooseMove(List.of(option)));
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

        @Test
        void factoryCreatesAPlayerOfTheRequestedColour() {
            assertEquals(Colour.GREEN, factory.createPlayer(Colour.GREEN).colour());
        }

        @Test
        void factoryCanCreateEveryColour() {
            for (Colour colour : Colour.values()) {
                assertEquals(4, factory.createPlayer(colour).piecesInBase());
            }
        }
    }

    @Nested
    class RedAggressiveCapture {

        private final Player red = factory.createPlayer(Colour.RED);
        private final Piece red1 = onBoard(Colour.RED, 1);
        private final Piece red2 = onBoard(Colour.RED, 2);
        private final Piece green1 = new Piece(Colour.GREEN, 1);
        private final Piece green2 = new Piece(Colour.GREEN, 2);

        @Test
        @DisplayName("Red prefers capturing to any other move")
        void capturesBeforeAnythingElse() {
            MoveOption capture = capture(red1, green1);
            assertSame(capture, red.chooseMove(List.of(move(red2), capture)));
        }

        @Test
        @DisplayName("Red captures the opponent piece closest to its home")
        void capturesTheVictimClosestToItsHome() {
            green1.enterBoard(Direction.CLOCKWISE);
            green2.moveTo(Position.inHomeStraight(3));
            MoveOption farVictim = capture(red1, green1);
            MoveOption nearVictim = capture(red2, green2);
            assertSame(nearVictim, red.chooseMove(List.of(farVictim, nearVictim)));
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

        @Test
        @DisplayName("Red forms a block only when it is unavoidable")
        void formsABlockWhenItIsTheOnlyMove() {
            MoveOption onlyMove = formingBlock(red1);
            assertSame(onlyMove, red.chooseMove(List.of(onlyMove)));
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
        @DisplayName("Green moving six to create a block beats leaving base")
        void formingABlockBeatsEntering() {
            MoveOption blockMaker = formingBlock(green1);
            assertSame(blockMaker, green.chooseMove(List.of(enter(green4), blockMaker)));
        }

        @Test
        @DisplayName("Green keeps an empty base: a six brings a piece out")
        void entersOnSix() {
            MoveOption entry = enter(green4);
            assertSame(entry, green.chooseMove(List.of(move(green1), entry)));
        }

        @Test
        @DisplayName("Green moves its other pieces before breaking a block")
        void movesOtherPiecesBeforeBreakingABlock() {
            MoveOption otherPiece = move(green3);
            assertSame(otherPiece, green.chooseMove(List.of(leavingBlock(green1), otherPiece)));
        }

        @Test
        @DisplayName("Rule T-4: Green uses the block move before breaking the block")
        void prefersBlockMoveToBreakingTheBlock() {
            MoveOption moveTogether = blockMove(green1, green2);
            assertSame(moveTogether, green.chooseMove(List.of(leavingBlock(green1), moveTogether)));
        }

        @Test
        @DisplayName("Green breaks a block only when nothing else can use the roll")
        void breaksTheBlockAsLastResort() {
            MoveOption breakAway = leavingBlock(green1);
            assertSame(breakAway, green.chooseMove(List.of(breakAway)));
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
        @DisplayName("Yellow does not chase captures it no longer needs")
        void ignoresCaptureByAPieceThatAlreadyCaptured() {
            yellow1.recordCapture();
            yellow2.moveTo(Position.inHomeStraight(1));
            MoveOption closestToHome = move(yellow2);
            assertSame(closestToHome, yellow.chooseMove(List.of(capture(yellow1, red1), closestToHome)));
        }

        @Test
        @DisplayName("Without captures, Yellow moves the piece closest to home")
        void movesThePieceClosestToHome() {
            yellow2.moveTo(Position.inHomeStraight(2));
            MoveOption closestToHome = move(yellow2);
            assertSame(closestToHome, yellow.chooseMove(List.of(move(yellow1), closestToHome)));
        }
    }

    @Nested
    class BlueCyclicMystery {

        private final CyclicMysteryStrategy blue = new CyclicMysteryStrategy();
        private final Piece blue1 = onBoard(Colour.BLUE, 1);
        private final Piece blue2 = onBoard(Colour.BLUE, 2);
        private final Piece blue3 = new Piece(Colour.BLUE, 3);

        BlueCyclicMystery() {
            blue3.enterBoard(Direction.COUNTER_CLOCKWISE);
        }

        @Test
        @DisplayName("Blue starts the cycle with B1")
        void startsWithPieceOne() {
            MoveOption first = move(blue1);
            assertSame(first, blue.chooseMove(List.of(move(blue2), first)));
        }

        @Test
        @DisplayName("Blue moves in a cycle: after B1 comes B2")
        void movesToTheNextPieceInTheCycle() {
            blue.chooseMove(List.of(move(blue1), move(blue2)));
            MoveOption second = move(blue2);
            assertSame(second, blue.chooseMove(List.of(move(blue1), second)));
        }

        @Test
        @DisplayName("Assumption A16: an unmovable piece is skipped")
        void skipsAPieceWithNoMoves() {
            blue.chooseMove(List.of(move(blue1)));
            MoveOption third = move(blue3);
            assertSame(third, blue.chooseMove(List.of(move(blue1), third)));
            assertEquals(4, blue.nextPieceNumber());
        }

        @Test
        @DisplayName("Blue moving counter-clockwise prefers landing on the mystery cell")
        void counterClockwisePieceSeeksTheMysteryCell() {
            blue.chooseMove(List.of(move(blue1)));
            blue.chooseMove(List.of(move(blue2)));
            MoveOption mystery = ontoMystery(blue3);
            assertSame(mystery, blue.chooseMove(List.of(move(blue3), mystery)));
        }

        @Test
        @DisplayName("Blue moving clockwise avoids landing on the mystery cell")
        void clockwisePieceAvoidsTheMysteryCell() {
            MoveOption safe = move(blue1);
            assertSame(safe, blue.chooseMove(List.of(ontoMystery(blue1), safe)));
        }

        @Test
        void factoryGivesBlueTheCyclicStrategy() {
            MoveOption first = move(blue1);
            assertSame(first, factory.createPlayer(Colour.BLUE).chooseMove(List.of(move(blue2), first)));
        }
    }

    private static Piece onBoard(Colour colour, int number) {
        Piece piece = new Piece(colour, number);
        piece.enterBoard(Direction.CLOCKWISE);
        return piece;
    }
}
