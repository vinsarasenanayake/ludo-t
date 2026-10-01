package com.ludot.movement;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCell;
import com.ludot.mystery.Teleporter;
import com.ludot.output.RecordingObserver;
import com.ludot.random.Coin;
import com.ludot.random.Dice;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveOption.Type;
import com.ludot.rules.MovePlanner;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandFactoryTest {

    private static final int BETA_FACE = 2;
    private static final MysteryCell NO_MYSTERY = MysteryCell.None.INSTANCE;

    private final Board board = new Board();
    private final RecordingObserver observer = new RecordingObserver();
    private final TrackNavigator navigator = new TrackNavigator();
    private final MovePlanner planner = new MovePlanner(board, navigator);
    private final Dice teleportDice = mock(Dice.class);
    private final Coin coin = mock(Coin.class);
    private final Piece red1 = new Piece(Colour.RED, 1);
    private final Piece red2 = new Piece(Colour.RED, 2);
    private final Piece green1 = new Piece(Colour.GREEN, 1);
    private final Piece green2 = new Piece(Colour.GREEN, 2);

    // R2 + T-1: entering puts the piece on its X and shows the status; the coin sets the direction
    @Test
    void enteringPlacesThePieceOnItsXWithTheCoinDirection() {
        GameCommand command = enter(red1, true);
        assertInstanceOf(EnterBoardCommand.class, command);
        assertEquals(Position.onTrack(26), red1.position());
        assertEquals(Direction.CLOCKWISE, red1.direction());
        assertTrue(observer.hasEvent("entered R1"));
        assertTrue(command.showsPlayerStatus());
        enter(red2, false);
        assertEquals(Direction.COUNTER_CLOCKWISE, red2.direction());
        verify(coin, times(2)).tossHeads();
    }

    // R6 + T-2: entering onto an opponent captures it and earns a bonus roll
    @Test
    void enteringOntoAnOpponentCapturesIt() {
        placeOnTrack(green1, 26);
        GameCommand command = enter(red1, true);
        assertTrue(green1.isInBase());
        assertTrue(command.grantsBonusRoll());
    }

    // R1 + T-1: a plain move reaches its destination and records passing the approach
    @Test
    void plainMoveReachesTheDestination() {
        placeOnTrack(red1, 22);
        GameCommand command = move(red1, 4);
        assertInstanceOf(MovePieceCommand.class, command);
        assertEquals(Position.onTrack(26), red1.position());
        assertTrue(observer.hasEvent("moved R1 to 26"));
        assertEquals(1, red1.approachPasses());
        assertFalse(command.grantsBonusRoll());
        assertFalse(command.showsPlayerStatus());
    }

    // R6 + T-2: a capture sends the victim to base and earns a bonus roll
    @Test
    void captureSendsVictimToBaseAndGivesBonus() {
        placeOnTrack(red1, 26);
        placeOnTrack(green1, 30);
        GameCommand command = move(red1, 4);
        assertTrue(green1.isInBase());
        assertTrue(board.occupantsAt(30).contains(red1));
        assertTrue(red1.hasCaptured());
        assertTrue(command.grantsBonusRoll());
        assertTrue(observer.hasEvent("capture R1 G1"));
    }

    // T-3: a blocked piece is reported and stops before the block
    @Test
    void blockedMoveIsReported() {
        placeOnTrack(red1, 0);
        placeOnTrack(green1, 4);
        placeOnTrack(green2, 4);
        move(red1, 6);
        assertEquals(Position.onTrack(3), red1.position());
        assertTrue(observer.hasEvent("blocked R1 by G1"));
        assertTrue(observer.hasEvent("moved before block RED 3"));
    }

    // T-3: a piece stuck right in front of a block reports it, and the throw is ignored
    @Test
    void fullyBlockedPieceIgnoresTheThrow() {
        placeOnTrack(red1, 3);
        placeOnTrack(green1, 4);
        placeOnTrack(green2, 4);
        GameCommand command = move(red1, 5);
        assertEquals(Position.onTrack(3), red1.position());
        assertTrue(observer.hasEvent("blocked R1 by G1"));
        assertTrue(observer.hasEvent("blocked throw ignored RED"));
        assertFalse(command.grantsBonusRoll());
    }

    // T-11: landing on the mystery cell teleports the piece
    @Test
    void landingOnMysteryCellTeleports() {
        placeOnTrack(red1, 26);
        MoveOption ontoMystery = option(List.of(red1), 4, Type.MOVE_PIECE, new MysteryCell.Active(30, 4));
        when(teleportDice.roll()).thenReturn(BETA_FACE);
        execute(factory(true).create(ontoMystery));
        assertEquals(Position.onTrack(25), red1.position());
    }

    // T-4: every piece of the block moves together
    @Test
    void everyPieceOfTheBlockMoves() {
        placeOnTrack(red1, 0);
        placeOnTrack(red2, 0);
        GameCommand command = moveBlock(6);
        assertInstanceOf(MoveBlockCommand.class, command);
        assertEquals(Position.onTrack(3), red1.position());
        assertEquals(Position.onTrack(3), red2.position());
        assertTrue(board.isBlockOwnedBy(3, Colour.RED));
    }

    // T-8: capturing a block counts as a capture for every capturing piece
    @Test
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

    // Design: the Null Object command only reports the ignored throw
    @Test
    void noMoveOnlyReportsTheIgnoredThrow() {
        GameCommand command = execute(factory(true).createNoMove(Colour.RED));
        assertInstanceOf(NullMoveCommand.class, command);
        assertTrue(observer.hasEvent("no move RED"));
        assertFalse(command.grantsBonusRoll());
        assertFalse(command.showsPlayerStatus());
    }

    // R10: the exact roll takes a piece from its home straight to home
    @Test
    void exactRollTakesThePieceHome() {
        red1.moveTo(Position.inHomeStraight(2));
        move(red1, 3);
        assertTrue(red1.isHome());
        assertTrue(observer.hasEvent("moved R1 to Home"));
    }

    // T-3: a counter-clockwise piece stops on the other side of the block
    @Test
    void counterClockwisePieceStopsOnTheOtherSideOfTheBlock() {
        placeOnTrack(red1, 8, Direction.COUNTER_CLOCKWISE);
        placeOnTrack(green1, 4);
        placeOnTrack(green2, 4);
        move(red1, 6);
        assertEquals(Position.onTrack(5), red1.position());
        assertTrue(observer.hasEvent("moved before block RED 5"));
    }

    // T-4: a block move reports the move of every piece in the block
    @Test
    void blockMoveReportsEveryPiece() {
        placeOnTrack(red1, 0);
        placeOnTrack(red2, 0);
        moveBlock(6);
        assertTrue(observer.hasEvent("moved R1 to 3"));
        assertTrue(observer.hasEvent("moved R2 to 3"));
    }

    // R2 + T-11: a piece entering onto the mystery cell is teleported at once
    @Test
    void enteringOntoTheMysteryCellTeleports() {
        MoveOption entry = option(List.of(red1), 6, Type.ENTER_BOARD, new MysteryCell.Active(26, 4));
        when(teleportDice.roll()).thenReturn(BETA_FACE);
        execute(factory(true).create(entry));
        assertEquals(Position.onTrack(25), red1.position());
        assertTrue(observer.hasEvent("teleport R1 BETA"));
    }

    // T-15: landing on Alpha or Gamma by a normal move gives no effect
    @Test
    void landingOnATeleportCellWithoutTeleportHasNoEffect() {
        placeOnTrack(red1, 3);
        move(red1, 4);
        assertEquals(Position.onTrack(7), red1.position());
        assertEquals(4, red1.adjustRoll(4));
        placeOnTrack(red2, 40);
        move(red2, 4);
        assertEquals(Position.onTrack(44), red2.position());
        assertEquals(Direction.CLOCKWISE, red2.direction());
        assertTrue(observer.events().stream().noneMatch(event -> event.startsWith("teleport")));
    }

    private GameCommand enter(Piece piece, boolean coinHeads) {
        MoveOption entry = option(List.of(piece), 6, Type.ENTER_BOARD, NO_MYSTERY);
        return execute(factory(coinHeads).create(entry));
    }

    private GameCommand move(Piece piece, int roll) {
        return execute(factory(true).create(option(List.of(piece), roll, Type.MOVE_PIECE, NO_MYSTERY)));
    }

    private GameCommand moveBlock(int roll) {
        MoveOption blockMove = option(List.of(red1, red2), roll, Type.MOVE_BLOCK, NO_MYSTERY);
        return execute(factory(true).create(blockMove));
    }

    private GameCommand execute(GameCommand command) {
        command.execute();
        return command;
    }

    private CommandFactory factory(boolean coinHeads) {
        when(coin.tossHeads()).thenReturn(coinHeads);
        Teleporter teleporter = new Teleporter(board, teleportDice, coin, navigator, observer);
        return new CommandFactory(board, coin, teleporter, observer);
    }

    private MoveOption option(List<Piece> pieces, int roll, Type type, MysteryCell mysteryCell) {
        return planner.findOptions(pieces, roll, mysteryCell).stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no " + type + " option"));
    }

    private void placeOnTrack(Piece piece, int cell) {
        placeOnTrack(piece, cell, Direction.CLOCKWISE);
    }

    private void placeOnTrack(Piece piece, int cell, Direction direction) {
        board.enter(piece, direction);
        board.move(piece, Position.onTrack(cell));
    }
}
