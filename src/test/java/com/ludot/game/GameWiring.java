package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.TrackNavigator;
import com.ludot.movement.CommandFactory;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.mystery.Teleporter;
import com.ludot.output.RecordingObserver;
import com.ludot.random.CellPicker;
import com.ludot.random.Coin;
import com.ludot.random.Dice;
import com.ludot.rules.MovePlanner;

final class GameWiring {

    private final Board board;
    private final RecordingObserver observer;

    GameWiring(Board board, RecordingObserver observer) {
        this.board = board;
        this.observer = observer;
    }

    MysteryCellManager mysteryCells(CellPicker cellPicker) {
        return new MysteryCellManager(board, cellPicker, observer);
    }

    RollResolver rollResolver(Dice dice, Coin coin, MysteryCellManager mysteryCells) {
        TrackNavigator navigator = new TrackNavigator();
        Teleporter teleporter = new Teleporter(board, dice, coin, navigator, observer);
        CommandFactory commands = new CommandFactory(board, coin, teleporter, observer);
        return new RollResolver(new MovePlanner(board, navigator), commands, mysteryCells);
    }

    TurnProcessor turnProcessor(Dice dice, Coin coin, MysteryCellManager mysteryCells) {
        return new TurnProcessor(dice, board, rollResolver(dice, coin, mysteryCells), observer);
    }
}
