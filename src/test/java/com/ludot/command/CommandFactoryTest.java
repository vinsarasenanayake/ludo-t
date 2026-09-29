package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.Route;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveOption.Landing;
import com.ludot.rules.MoveOption.Type;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;
import com.ludot.testsupport.TestDoubles.RecordingObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.testsupport.TestDoubles.fixedCoin;
import static com.ludot.testsupport.TestDoubles.scriptedDice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandFactoryTest {

    private static final int BETA_FACE = 2;

    private final Board board = new Board();
    private final RecordingObserver observer = new RecordingObserver();
    private final TrackNavigator navigator = new TrackNavigator();
    private final MovePlanner planner = new MovePlanner(board, navigator);
    private Piece red1;
    private Piece red2;
    private Piece green1;
    private Piece green2;

    @BeforeEach
    void setUp() {
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
        green1 = new Piece(Colour.GREEN, 1);
        green2 = new Piece(Colour.GREEN, 2);
    }

    @Test
    void enteringPlacesThePieceOnItsStartCell() {
        enter(true);
        assertEquals(Position.onTrack(26), red1.position());
    }

    @Test
    @DisplayName("Rule T-1: heads means clockwise")
    void headsMeansClockwise() {
        enter(true);
        assertEquals(Direction.CLOCKWISE, red1.direction());
    }

    @Test
    @DisplayName("Rule T-1: tails means counter-clockwise")
    void tailsMeansCounterClockwise() {
        enter(false);
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    @Test
    void entryIsReportedWithThePlayerStatus() {
        GameCommand command = enter(true);
        assertTrue(observer.hasEvent("entered R1"));
        assertTrue(command.showsPlayerStatus());
    }

    @Test
    @DisplayName("Rule 6 + T-2: entering onto an opponent captures it and earns a bonus roll")
    void enteringOntoAnOpponentCapturesIt() {
        placeOnTrack(green1, 26);
        GameCommand command = enter(true);
        assertTrue(green1.isInBase());
        assertTrue(command.grantsBonusRoll());
    }

    @Test
    void pieceMovesToTheDestination() {
        placeOnTrack(red1, 26);
        move(4);
        assertEquals(Position.onTrack(30), red1.position());
        assertTrue(observer.hasEvent("moved R1 to 30"));
    }

    @Test
    void plainMoveGivesNoBonusRollAndNoStatus() {
        placeOnTrack(red1, 26);
        GameCommand command = move(4);
        assertFalse(command.grantsBonusRoll());
        assertFalse(command.showsPlayerStatus());
    }

    @Test
    @DisplayName("Rule 6 + T-2: a capture sends the victim to base and earns a bonus roll")
    void captureSendsVictimHomeAndGivesBonus() {
        placeOnTrack(red1, 26);
        placeOnTrack(green1, 30);
        GameCommand command = move(4);
        assertTrue(green1.isInBase());
        assertTrue(board.occupantsAt(30).contains(red1));
        assertEquals(1, red1.captureCount());
        assertTrue(command.grantsBonusRoll());
        assertTrue(observer.hasEvent("capture R1 G1"));
    }

    @Test
    @DisplayName("Rule T-3: a blocked piece is reported and stops before the block")
    void blockedMoveIsReported() {
        placeOnTrack(red1, 0);
        placeOnTrack(green1, 4);
        placeOnTrack(green2, 4);
        move(6);
        assertEquals(Position.onTrack(3), red1.position());
        assertTrue(observer.hasEvent("blocked R1 by G1"));
    }

    @Test
    void passingTheApproachIsRecorded() {
        placeOnTrack(red1, 22);
        move(3);
        assertEquals(1, red1.approachPasses());
    }

    @Test
    void everyApproachPassOfTheRouteIsRecorded() {
        Route route = Route.completed(Position.onTrack(20), Position.onTrack(26), 6, 2);
        factory(true).create(new MoveOption(Type.MOVE_PIECE, List.of(red1), route, Landing.offTrack(), false)).execute();
        assertEquals(2, red1.approachPasses());
    }

    @Test
    @DisplayName("Rule T-11: landing on the mystery cell teleports the piece")
    void landingOnMysteryCellTeleports() {
        placeOnTrack(red1, 26);
        factory(true, BETA_FACE).create(option(List.of(red1), 4, Type.MOVE_PIECE, new MysteryCell.Active(30, 4))).execute();
        assertEquals(Position.onTrack(25), red1.position());
    }

    @Test
    @DisplayName("Rule T-4: every piece of the block moves together")
    void everyPieceOfTheBlockMoves() {
        placeOnTrack(red1, 0);
        placeOnTrack(red2, 0);
        moveBlock(6);
        assertEquals(Position.onTrack(3), red1.position());
        assertEquals(Position.onTrack(3), red2.position());
        assertTrue(board.isBlockAt(3));
    }

    @Test
    @DisplayName("Rule T-8: capturing a block gives every capturing piece a capture")
    void capturingABlockCountsForEveryPiece() {
        placeOnTrack(red1, 0);
        placeOnTrack(red2, 0);
        placeOnTrack(green1, 3);
        placeOnTrack(green2, 3);
        GameCommand command = moveBlock(6);
        assertTrue(green1.isInBase());
        assertTrue(green2.isInBase());
        assertEquals(1, red1.captureCount());
        assertEquals(1, red2.captureCount());
        assertTrue(command.grantsBonusRoll());
    }

    @Test
    @DisplayName("Null Object: no legal move just reports the ignored throw")
    void noMoveReportsTheIgnoredThrow() {
        GameCommand command = factory(true).createNoMove(Colour.RED);
        command.execute();
        assertTrue(observer.hasEvent("no move RED"));
    }

    @Test
    @DisplayName("Null Object: no move gives no bonus roll and no status")
    void noMoveChangesNothing() {
        GameCommand command = factory(true).createNoMove(Colour.RED);
        assertFalse(command.grantsBonusRoll());
        assertFalse(command.showsPlayerStatus());
    }

    private GameCommand enter(boolean coinHeads) {
        return execute(factory(coinHeads).create(option(List.of(red1), 6, Type.ENTER_BOARD, MysteryCell.None.INSTANCE)));
    }

    private GameCommand move(int roll) {
        return execute(factory(true).create(option(List.of(red1), roll, Type.MOVE_PIECE, MysteryCell.None.INSTANCE)));
    }

    private GameCommand moveBlock(int roll) {
        return execute(factory(true).create(option(List.of(red1, red2), roll, Type.MOVE_BLOCK, MysteryCell.None.INSTANCE)));
    }

    private GameCommand execute(GameCommand command) {
        command.execute();
        return command;
    }

    private CommandFactory factory(boolean coinHeads, int... teleportRolls) {
        Teleporter teleporter = new Teleporter(board, scriptedDice(teleportRolls), fixedCoin(coinHeads), navigator, observer);
        return new CommandFactory(board, fixedCoin(coinHeads), navigator, teleporter, observer);
    }

    private MoveOption option(List<Piece> pieces, int roll, Type type, MysteryCell mysteryCell) {
        return planner.findOptions(pieces, roll, mysteryCell).stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no " + type + " option"));
    }

    private void placeOnTrack(Piece piece, int cell) {
        board.enter(piece, Direction.CLOCKWISE);
        board.move(piece, Position.onTrack(cell));
    }
}
