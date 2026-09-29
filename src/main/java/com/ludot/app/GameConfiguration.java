package com.ludot.app;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.TrackNavigator;
import com.ludot.game.GameEngine;
import com.ludot.game.LudoGameFacade;
import com.ludot.game.RollResolver;
import com.ludot.game.TurnOrderResolver;
import com.ludot.game.TurnProcessor;
import com.ludot.movement.CommandFactory;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.mystery.Teleporter;
import com.ludot.output.ConsoleReporter;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.random.SeededRandomness;
import com.ludot.rules.MovePlanner;

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
