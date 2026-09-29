package com.ludot.app;

import com.ludot.command.CommandFactory;
import com.ludot.command.LandingHandler;
import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.engine.BriefingMonitor;
import com.ludot.engine.GameEngine;
import com.ludot.engine.RollResolver;
import com.ludot.engine.StatusSnapshotFactory;
import com.ludot.engine.TurnOrderResolver;
import com.ludot.engine.TurnProcessor;
import com.ludot.infrastructure.ConsoleGameReporter;
import com.ludot.infrastructure.CsvGameResultGateway;
import com.ludot.infrastructure.FairCoin;
import com.ludot.infrastructure.MessageFormatter;
import com.ludot.infrastructure.RandomCellPicker;
import com.ludot.infrastructure.SixSidedDice;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.port.Coin;
import com.ludot.port.Dice;
import com.ludot.port.GameObserver;
import com.ludot.port.GameResultGateway;
import com.ludot.rules.CaptureResolver;
import com.ludot.rules.EffectFactory;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.MysteryCellManager;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameConfiguration {

    private static final Path RESULTS_FILE = Path.of("game-results.csv");

    private final long seed;
    private final PrintStream out;
    private final GameResultGateway results;

    public GameConfiguration(long seed, PrintStream out) {
        this(seed, out, new CsvGameResultGateway(RESULTS_FILE));
    }

    public GameConfiguration(long seed, PrintStream out, GameResultGateway results) {
        this.seed = seed;
        this.out = out;
        this.results = results;
    }

    public LudoGameFacade createGame() {
        Random random = new Random(seed);
        Dice dice = new SixSidedDice(random);
        Coin coin = new FairCoin(random);
        GameObserver observer = new ConsoleGameReporter(new MessageFormatter(), out);

        Board board = new Board();
        TrackNavigator navigator = new TrackNavigator();
        CaptureResolver captureResolver = new CaptureResolver(board);
        MovePlanner planner = new MovePlanner(board, navigator, captureResolver);
        Teleporter teleporter = new Teleporter(board, dice, coin, navigator, new EffectFactory(), observer);
        LandingHandler landingHandler = new LandingHandler(captureResolver, teleporter, observer);
        CommandFactory commands = new CommandFactory(board, coin, navigator, landingHandler, observer);
        MysteryCellManager mysteryCells = new MysteryCellManager(board, new RandomCellPicker(random));
        StatusSnapshotFactory snapshots = new StatusSnapshotFactory();

        RollResolver rollResolver = new RollResolver(planner, commands, mysteryCells);
        TurnProcessor turnProcessor = new TurnProcessor(dice, rollResolver,
                new BriefingMonitor(board, observer), snapshots, observer);
        GameEngine engine = new GameEngine(createPlayers(navigator), turnProcessor, mysteryCells, snapshots, observer);

        return new LudoGameFacade(engine, new TurnOrderResolver(dice, observer), results, seed);
    }

    private List<Player> createPlayers(TrackNavigator navigator) {
        PlayerFactory playerFactory = new PlayerFactory(navigator);
        return Arrays.stream(Colour.values())
                .map(playerFactory::createPlayer)
                .toList();
    }
}