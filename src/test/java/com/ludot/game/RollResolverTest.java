package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.PieceEffect;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.movement.GameCommand;
import com.ludot.output.RecordingObserver;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ludot.random.RandomMocks.coinLanding;
import static com.ludot.random.RandomMocks.diceRolling;
import static com.ludot.random.RandomMocks.pickerChoosing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RollResolverTest {

    private final RecordingObserver observer = new RecordingObserver();
    private final Board board = new Board();
    private final GameWiring wiring = new GameWiring(board, observer);
    private final PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
    private final Player red = playerFactory.createPlayer(Colour.RED);
    private final Player green = playerFactory.createPlayer(Colour.GREEN);
    private final RollResolver resolver =
            wiring.rollResolver(diceRolling(1), coinLanding(true), wiring.mysteryCells(pickerChoosing(0)));

    // Null Object: with no legal move the resolver still returns a command, which only reports the ignored throw
    @Test
    void noLegalMoveGivesACommandThatIgnoresTheThrow() {
        GameCommand command = resolver.commandFor(red, 4);
        command.execute();
        assertTrue(observer.hasEvent("no move RED"));
        assertFalse(command.grantsBonusRoll());
    }

    // T-6: every piece of a blockade but one must break away
    @Test
    void allPiecesButOneBreakAwayFromABlockade() {
        Piece red2 = red.pieces().get(1);
        Piece red3 = red.pieces().get(2);
        board.enter(red.pieces().get(0), Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        board.enter(red3, Direction.CLOCKWISE);
        assertEquals(List.of(red2, red3), resolver.piecesToBreakAway(red));
    }

    // T-5 + T-6: a breakaway piece moves six cells in its own original direction
    @Test
    void breakawayPieceMovesSixInItsOwnDirection() {
        Piece red2 = red.pieces().get(1);
        board.enter(red.pieces().get(0), Direction.CLOCKWISE);
        board.enter(red2, Direction.COUNTER_CLOCKWISE);
        resolver.breakAwayCommand(red2).orElseThrow().execute();
        assertEquals(Position.onTrack(20), red2.position());
    }

    // T-6 + A10: a breakaway is exactly six cells, even for an energised piece
    @Test
    void energisedPieceStillBreaksAwayExactlySix() {
        Piece red2 = red.pieces().get(1);
        board.enter(red.pieces().get(0), Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        red2.applyEffect(new PieceEffect.Energised());
        resolver.breakAwayCommand(red2).orElseThrow().execute();
        assertEquals(Position.onTrack(32), red2.position());
    }

    // T-6 + T-3: a breakaway piece blocked on the way stops in front of the block
    @Test
    void breakawayPieceStopsInFrontOfAnOpponentBlock() {
        Piece red2 = red.pieces().get(1);
        board.enter(red.pieces().get(0), Direction.CLOCKWISE);
        board.enter(red2, Direction.CLOCKWISE);
        Piece green1 = green.pieces().get(0);
        Piece green2 = green.pieces().get(1);
        board.enter(green1, Direction.CLOCKWISE);
        board.enter(green2, Direction.CLOCKWISE);
        board.move(green1, Position.onTrack(30));
        board.move(green2, Position.onTrack(30));
        resolver.breakAwayCommand(red2).orElseThrow().execute();
        assertEquals(Position.onTrack(29), red2.position());
    }
}
