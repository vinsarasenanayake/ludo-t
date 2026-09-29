package com.ludot.app;

import com.ludot.command.CommandFactory;
import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.engine.GameEngine;
import com.ludot.engine.RollResolver;
import com.ludot.engine.TurnOrderResolver;
import com.ludot.engine.TurnProcessor;
import com.ludot.infrastructure.ConsoleReporter;
import com.ludot.infrastructure.SeededRandomness;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.MysteryCellManager;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class GameConfiguration {

    private final long seed;
    private final PrintStream out;

    public GameConfiguration(long seed, PrintStream out) {
        this.seed = seed;
        this.out = out;
    }

    public LudoGameFacade createGame() {
        SeededRandomness random = new SeededRandomness(seed);
        ConsoleReporter reporter = new ConsoleReporter(out);
        Board board = new Board();
        TrackNavigator navigator = new TrackNavigator();
        Teleporter teleporter = new Teleporter(board, random, random, navigator, reporter);
        CommandFactory commands = new CommandFactory(board, random, navigator, teleporter, reporter);
        MysteryCellManager mysteryCells = new MysteryCellManager(board, random, reporter);
        RollResolver rollResolver = new RollResolver(new MovePlanner(board, navigator), commands, mysteryCells);
        TurnProcessor turnProcessor = new TurnProcessor(random, board, rollResolver, reporter);
        GameEngine engine = new GameEngine(createPlayers(navigator), turnProcessor, mysteryCells, reporter);
        return new LudoGameFacade(engine, new TurnOrderResolver(random, reporter));
    }

    private List<Player> createPlayers(TrackNavigator navigator) {
        PlayerFactory playerFactory = new PlayerFactory(navigator);
        return Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();
    }
}
