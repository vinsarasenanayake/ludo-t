package com.ludot.app;

import com.ludot.command.CommandFactory;
import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.dto.GameResultDto;
import com.ludot.engine.GameEngine;
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

public class LudoGameFacade {

    private final GameEngine engine;
    private final TurnOrderResolver turnOrderResolver;

    public LudoGameFacade(long seed, PrintStream out) {
        SeededRandomness random = new SeededRandomness(seed);
        ConsoleReporter reporter = new ConsoleReporter(out);
        Board board = new Board();
        TrackNavigator navigator = new TrackNavigator();
        Teleporter teleporter = new Teleporter(board, random, random, navigator, reporter);
        CommandFactory commands = new CommandFactory(board, random, navigator, teleporter, reporter);
        MysteryCellManager mysteryCells = new MysteryCellManager(board, random, reporter);
        TurnProcessor turnProcessor = new TurnProcessor(random, board, new MovePlanner(board, navigator),
                commands, mysteryCells, reporter);
        PlayerFactory playerFactory = new PlayerFactory(navigator);
        List<Player> players = Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();
        this.engine = new GameEngine(players, turnProcessor, mysteryCells, reporter);
        this.turnOrderResolver = new TurnOrderResolver(random, reporter);
    }

    public GameResultDto play() {
        engine.introducePlayers();
        return engine.run(turnOrderResolver.resolve(List.of(Colour.values())));
    }
}
