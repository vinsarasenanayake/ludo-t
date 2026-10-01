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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LudoGameFacadeTest {

    private final RecordingObserver observer = new RecordingObserver();

    // Facade + Brief 3.1: one call introduces the players, then decides the turn order, then plays the game
    @Test
    void playIntroducesPlayersThenDecidesTheOrder() {
        facade().play();
        List<String> events = observer.events();
        assertEquals("introduced RED", events.get(0));
        assertEquals("introduced BLUE", events.get(3));
        assertTrue(events.get(4).startsWith("opening roll"));
    }

    // Facade: the caller gets the whole result back from a single call
    @Test
    void playReturnsTheFinalRanking() {
        GameResultDto result = facade().play();
        assertEquals(4, result.finishingOrder().size());
        assertTrue(observer.hasEvent("game over " + result.finishingOrder()));
    }

    private LudoGameFacade facade() {
        Board board = new Board();
        GameWiring wiring = new GameWiring(board, observer);
        SeededRandomness random = new SeededRandomness(1);
        PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
        List<Player> players = Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();
        MysteryCellManager mysteryCells = wiring.mysteryCells(random);
        TurnProcessor turns = wiring.turnProcessor(random, random, mysteryCells);
        GameEngine engine = new GameEngine(players, turns, mysteryCells, observer);
        return new LudoGameFacade(engine, new TurnOrderResolver(random, observer));
    }
}
