package com.ludot.engine;

import com.ludot.command.CommandFactory;
import com.ludot.command.LandingHandler;
import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.player.Player;
import com.ludot.player.PlayerFactory;
import com.ludot.port.CellPicker;
import com.ludot.port.Coin;
import com.ludot.port.Dice;
import com.ludot.rules.CaptureResolver;
import com.ludot.rules.EffectFactory;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.MysteryCellManager;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;
import com.ludot.testsupport.RecordingObserver;

import java.util.Arrays;
import java.util.List;

class EngineTestFixture {

    final Board board = new Board();
    final RecordingObserver observer = new RecordingObserver();
    final StatusSnapshotFactory snapshots = new StatusSnapshotFactory();
    final MysteryCellManager mysteryCells;
    final RollResolver rollResolver;
    final TurnProcessor turnProcessor;
    final List<Player> players;

    EngineTestFixture(Dice dice, Coin coin, CellPicker cellPicker) {
        TrackNavigator navigator = new TrackNavigator();
        CaptureResolver captureResolver = new CaptureResolver(board);
        Teleporter teleporter = new Teleporter(board, dice, coin, navigator, new EffectFactory(), observer);
        CommandFactory commands = new CommandFactory(board, coin, navigator,
                new LandingHandler(captureResolver, teleporter, observer), observer);
        mysteryCells = new MysteryCellManager(board, cellPicker);
        rollResolver = new RollResolver(new MovePlanner(board, navigator, captureResolver), commands, mysteryCells);
        turnProcessor = new TurnProcessor(dice, rollResolver, new BriefingMonitor(board, observer), snapshots, observer);
        PlayerFactory playerFactory = new PlayerFactory(navigator);
        players = Arrays.stream(Colour.values()).map(playerFactory::createPlayer).toList();
    }

    Player player(Colour colour) {
        return players.stream().filter(player -> player.colour() == colour).findFirst().orElseThrow();
    }

    GameEngine engine() {
        return new GameEngine(players, turnProcessor, mysteryCells, snapshots, observer);
    }
}