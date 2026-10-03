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

    @Test
    void breakawayPiecesShareSixUnitsAndDoNotReformTheBlockade() {
        Piece red1 = enterRed(0, Direction.CLOCKWISE);
        Piece red2 = enterRed(1, Direction.CLOCKWISE);
        Piece red3 = enterRed(2, Direction.CLOCKWISE);
        breakAway();
        assertEquals(List.of(Position.onTrack(26), Position.onTrack(30), Position.onTrack(28)),
                List.of(red1.position(), red2.position(), red3.position()));
    }

    @Test
    void breakawayPieceMovesSixInItsOwnDirection() {
        enterRed(0, Direction.CLOCKWISE);
        Piece red2 = enterRed(1, Direction.COUNTER_CLOCKWISE);
        breakAway();
        assertEquals(Position.onTrack(20), red2.position());
    }

    @Test
    void energisedPieceStillBreaksAwayExactlySix() {
        enterRed(0, Direction.CLOCKWISE);
        Piece red2 = enterRed(1, Direction.CLOCKWISE);
        red2.applyEffect(new PieceEffect.Energised());
        breakAway();
        assertEquals(Position.onTrack(32), red2.position());
    }

    @Test
    void breakawayPieceThatWouldBeCutShortStaysInPlace() {
        enterRed(0, Direction.CLOCKWISE);
        enterRed(1, Direction.CLOCKWISE);
        placeGreenBlockAt();
        assertTrue(breakawayCommands().isEmpty());
    }

    @Test
    void breakawayMayLandOnAnOwnPieceWhenThatIsTheOnlyWayOut() {
        enterRed(0, Direction.CLOCKWISE);
        Piece red2 = enterRed(1, Direction.CLOCKWISE);
        board.move(enterRed(2, Direction.CLOCKWISE), Position.onTrack(32));
        breakAway();
        assertEquals(Position.onTrack(32), red2.position());
    }

    @Test
    void noBlockadeMeansNoBreakaway() {
        enterRed(0, Direction.CLOCKWISE);
        assertTrue(breakawayCommands().isEmpty());
    }

    private Piece enterRed(int index, Direction direction) {
        Piece piece = red.pieces().get(index);
        board.enter(piece, direction);
        return piece;
    }

    private void placeGreenBlockAt() {
        for (Piece piece : green.pieces().subList(0, 2)) {
            board.enter(piece, Direction.CLOCKWISE);
            board.move(piece, Position.onTrack(30));
        }
    }

    private List<GameCommand> breakawayCommands() {
        return resolver.breakaways(red).stream()
                .flatMap(breakaway -> resolver.breakAwayCommand(breakaway).stream())
                .toList();
    }

    private void breakAway() {
        for (RollResolver.Breakaway breakaway : resolver.breakaways(red)) {
            resolver.breakAwayCommand(breakaway).ifPresent(GameCommand::execute);
        }
    }
}