package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.output.RecordingObserver;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.random.SeededRandomness;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.ludot.random.RandomMocks.coinLanding;
import static com.ludot.random.RandomMocks.diceRolling;
import static com.ludot.random.RandomMocks.pickerChoosing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineTest {

    private static final int FIRST_ROUND = 1;
    private static final int UNCHANGED_ROUNDS_BEFORE_STALL = 50;
    private static final List<Colour> TURN_ORDER = List.of(Colour.RED, Colour.GREEN, Colour.YELLOW, Colour.BLUE);

    private final RecordingObserver observer = new RecordingObserver();
    private final Board board = new Board();
    private final GameWiring wiring = new GameWiring(board, observer);
    private final PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
    private final List<Player> players = Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();

    // R11: a full seeded game ends once three players are home, and ranks all four
    @Test
    void fullGameRanksAllFourPlayers() {
        SeededRandomness random = new SeededRandomness(1);
        MysteryCellManager mysteryCells = wiring.mysteryCells(random);
        TurnProcessor turns = wiring.turnProcessor(random, random, mysteryCells);
        GameResultDto result = new GameEngine(players, turns, mysteryCells, observer).run(TURN_ORDER);
        assertEquals(4, result.finishingOrder().size());
        assertTrue(observer.hasEvent("finished " + result.finishingOrder().get(0) + " 1"));
    }

    // Brief 3.1: the piece locations are shown after every round
    @Test
    void everyRoundEndsWithASummary() {
        GameResultDto result = stalledGame();
        long summaries = observer.events().stream().filter("round ended"::equals).count();
        assertEquals(result.rounds(), summaries);
    }

    // Interpretation: a game in which no piece can ever move stops after 50 unchanged rounds
    @Test
    void gameWithoutProgressIsStalled() {
        GameResultDto result = stalledGame();
        assertEquals(FIRST_ROUND + UNCHANGED_ROUNDS_BEFORE_STALL, result.rounds());
    }

    // R11 (interpretation): a stalled game gives no place to a player who never got all its pieces home
    @Test
    void stalledGameDoesNotRankUnfinishedPlayers() {
        GameResultDto result = stalledGame();
        assertTrue(result.finishingOrder().isEmpty());
    }

    private GameResultDto stalledGame() {
        MysteryCellManager mysteryCells = wiring.mysteryCells(pickerChoosing(0));
        TurnProcessor turns = wiring.turnProcessor(diceRolling(1), coinLanding(true), mysteryCells);
        return new GameEngine(players, turns, mysteryCells, observer).run(TURN_ORDER);
    }
}
