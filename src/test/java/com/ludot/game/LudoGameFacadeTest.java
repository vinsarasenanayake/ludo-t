package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.output.RecordingListener;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.random.SeededRandomness;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LudoGameFacadeTest {

    private final RecordingListener listener = new RecordingListener();

    @Test
    void playIntroducesPlayersThenDecidesTheOrder() {
        facade().play();
        List<String> events = listener.events();
        assertEquals("introduced RED", events.get(0));
        assertEquals("introduced BLUE", events.get(3));
        assertTrue(events.get(4).startsWith("opening roll"));
    }

    private LudoGameFacade facade() {
        Board board = new Board();
        GameWiring wiring = new GameWiring(board, listener);
        SeededRandomness random = new SeededRandomness(1);
        PlayerFactory playerFactory = new PlayerFactory(new TrackNavigator());
        List<Player> players = Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();
        MysteryCellManager mysteryCells = wiring.mysteryCells(random);
        TurnProcessor turns = wiring.turnProcessor(random, random, mysteryCells);
        GameEngine engine = new GameEngine(players, turns, mysteryCells, listener);
        return new LudoGameFacade(engine, new TurnOrderResolver(random, listener));
    }
}