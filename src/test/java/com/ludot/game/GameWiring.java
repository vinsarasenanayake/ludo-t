package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.TrackNavigator;
import com.ludot.movement.CommandFactory;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.mystery.Teleporter;
import com.ludot.output.RecordingListener;
import com.ludot.random.CellPicker;
import com.ludot.random.Coin;
import com.ludot.random.Dice;
import com.ludot.rules.MovePlanner;

// Builds real game parts around the chosen random sources
final class GameWiring {

    private final Board board;
    private final RecordingListener listener;

    GameWiring(Board board, RecordingListener listener) {
        this.board = board;
        this.listener = listener;
    }

    MysteryCellManager mysteryCells(CellPicker cellPicker) {
        return new MysteryCellManager(board, cellPicker, listener);
    }

    RollResolver rollResolver(Dice dice, Coin coin, MysteryCellManager mysteryCells) {
        TrackNavigator navigator = new TrackNavigator();
        Teleporter teleporter = new Teleporter(board, dice, coin, navigator, listener);
        CommandFactory commands = new CommandFactory(board, coin, teleporter, listener);
        return new RollResolver(new MovePlanner(board, navigator), commands, mysteryCells);
    }

    TurnProcessor turnProcessor(Dice dice, Coin coin, MysteryCellManager mysteryCells) {
        return new TurnProcessor(dice, board, rollResolver(dice, coin, mysteryCells), listener);
    }
}