package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCell;
import com.ludot.mystery.Teleporter;
import com.ludot.random.Dice;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveOption.Type;
import com.ludot.rules.MovePlanner;
import com.ludot.testsupport.TestDoubles.RecordingObserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.testsupport.TestDoubles.fixedCoin;
import static com.ludot.testsupport.TestDoubles.scriptedDice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    @DisplayName("R2 + T-1: entering puts the piece on its X and shows the status; the coin sets the direction")
    void enteringPlacesThePieceOnItsXWithTheCoinDirection() {
        GameCommand command = enter(true);
        assertInstanceOf(EnterBoardCommand.class, command);
        assertEquals(Position.onTrack(26), red1.position());
        assertEquals(Direction.CLOCKWISE, red1.direction());
        assertTrue(observer.hasEvent("entered R1"));
        assertTrue(command.showsPlayerStatus());
        factory(false).create(option(List.of(red2), 6, Type.ENTER_BOARD, MysteryCell.None.INSTANCE)).execute();
        assertEquals(Direction.COUNTER_CLOCKWISE, red2.direction());
    }

    @Test
    @DisplayName("R6 + T-2: entering onto an opponent captures it and earns a bonus roll")
    void enteringOntoAnOpponentCapturesIt() {
        placeOnTrack(green1, 26);
        GameCommand command = enter(true);
        assertTrue(green1.isInBase());
        assertTrue(command.grantsBonusRoll());
    }

    @Test
    @DisplayName("R1 + T-1: a plain move reaches its destination and records passing the approach")
    void plainMoveReachesTheDestination() {
        placeOnTrack(red1, 22);
        GameCommand command = move(4);
        assertInstanceOf(MovePieceCommand.class, command);
        assertEquals(Position.onTrack(26), red1.position());
        assertTrue(observer.hasEvent("moved R1 to 26"));
        assertEquals(1, red1.approachPasses());
        assertFalse(command.grantsBonusRoll());
        assertFalse(command.showsPlayerStatus());
    }

    @Test
    @DisplayName("R6 + T-2: a capture sends the victim to base and earns a bonus roll")
    void captureSendsVictimHomeAndGivesBonus() {
        placeOnTrack(red1, 26);
        placeOnTrack(green1, 30);
        GameCommand command = move(4);
        assertTrue(green1.isInBase());
        assertTrue(board.occupantsAt(30).contains(red1));
        assertTrue(red1.hasCaptured());
        assertTrue(command.grantsBonusRoll());
        assertTrue(observer.hasEvent("capture R1 G1"));
    }

    @Test
    @DisplayName("T-3: a blocked piece is reported and stops before the block")
    void blockedMoveIsReported() {
        placeOnTrack(red1, 0);
        placeOnTrack(green1, 4);
        placeOnTrack(green2, 4);
        move(6);
        assertEquals(Position.onTrack(3), red1.position());
        assertTrue(observer.hasEvent("blocked R1 by G1"));
    }

    @Test
    @DisplayName("T-3: a piece stuck right in front of a block reports it, and the throw is ignored")
    void fullyBlockedPieceIgnoresTheThrow() {
        placeOnTrack(red1, 3);
        placeOnTrack(green1, 4);
        placeOnTrack(green2, 4);
        GameCommand command = move(5);
        assertEquals(Position.onTrack(3), red1.position());
        assertTrue(observer.hasEvent("blocked R1 by G1"));
        assertTrue(observer.hasEvent("blocked throw ignored RED"));
        assertFalse(command.grantsBonusRoll());
    }

    @Test
    @DisplayName("T-11: landing on the mystery cell teleports the piece")
    void landingOnMysteryCellTeleports() {
        placeOnTrack(red1, 26);
        MoveOption ontoMystery = option(List.of(red1), 4, Type.MOVE_PIECE, new MysteryCell.Active(30, 4));
        factory(true, BETA_FACE).create(ontoMystery).execute();
        assertEquals(Position.onTrack(25), red1.position());
    }

    @Test
    @DisplayName("T-4: every piece of the block moves together")
    void everyPieceOfTheBlockMoves() {
        placeOnTrack(red1, 0);
        placeOnTrack(red2, 0);
        GameCommand command = moveBlock(6);
        assertInstanceOf(MoveBlockCommand.class, command);
        assertEquals(Position.onTrack(3), red1.position());
        assertEquals(Position.onTrack(3), red2.position());
        assertTrue(board.isBlockAt(3));
    }

    @Test
    @DisplayName("T-8: capturing a block counts as a capture for every capturing piece")
    void capturingABlockCountsForEveryPiece() {
        placeOnTrack(red1, 0);
        placeOnTrack(red2, 0);
        placeOnTrack(green1, 3);
        placeOnTrack(green2, 3);
        GameCommand command = moveBlock(6);
        assertTrue(green1.isInBase());
        assertTrue(green2.isInBase());
        assertTrue(red1.hasCaptured());
        assertTrue(red2.hasCaptured());
        assertTrue(command.grantsBonusRoll());
    }

    @Test
    @DisplayName("Design: the Null Object command only reports the ignored throw")
    void noMoveOnlyReportsTheIgnoredThrow() {
        GameCommand command = execute(factory(true).createNoMove(Colour.RED));
        assertInstanceOf(NullMoveCommand.class, command);
        assertTrue(observer.hasEvent("no move RED"));
        assertFalse(command.grantsBonusRoll());
        assertFalse(command.showsPlayerStatus());
    }

    private GameCommand enter(boolean coinHeads) {
        MoveOption entry = option(List.of(red1), 6, Type.ENTER_BOARD, MysteryCell.None.INSTANCE);
        return execute(factory(coinHeads).create(entry));
    }

    private GameCommand move(int roll) {
        return execute(factory(true).create(option(List.of(red1), roll, Type.MOVE_PIECE, MysteryCell.None.INSTANCE)));
    }

    private GameCommand moveBlock(int roll) {
        MoveOption blockMove = option(List.of(red1, red2), roll, Type.MOVE_BLOCK, MysteryCell.None.INSTANCE);
        return execute(factory(true).create(blockMove));
    }

    private GameCommand execute(GameCommand command) {
        command.execute();
        return command;
    }

    private CommandFactory factory(boolean coinHeads, int... teleportRolls) {
        Dice teleportDice = scriptedDice(teleportRolls);
        Teleporter teleporter = new Teleporter(board, teleportDice, fixedCoin(coinHeads), navigator, observer);
        return new CommandFactory(board, fixedCoin(coinHeads), teleporter, observer);
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
