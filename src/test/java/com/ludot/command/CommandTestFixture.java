package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.NoMysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.CaptureResolver;
import com.ludot.rules.EffectFactory;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.MoveType;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;
import com.ludot.testsupport.FixedCoin;
import com.ludot.testsupport.RecordingObserver;
import com.ludot.testsupport.ScriptedDice;

import java.util.List;

class CommandTestFixture {

    final Board board = new Board();
    final RecordingObserver observer = new RecordingObserver();
    final TrackNavigator navigator = new TrackNavigator();
    final CaptureResolver captureResolver = new CaptureResolver(board);
    final MovePlanner planner = new MovePlanner(board, navigator, captureResolver);

    CommandFactory factoryWith(boolean coinHeads, int... teleportRolls) {
        Teleporter teleporter = new Teleporter(board, new ScriptedDice(teleportRolls), new FixedCoin(coinHeads),
                navigator, new EffectFactory(), observer);
        LandingHandler landingHandler = new LandingHandler(captureResolver, teleporter, observer);
        return new CommandFactory(board, new FixedCoin(coinHeads), navigator, landingHandler, observer);
    }

    MoveOption option(List<Piece> pieces, int roll, MoveType type) {
        return option(pieces, roll, type, NoMysteryCell.INSTANCE);
    }

    MoveOption option(List<Piece> pieces, int roll, MoveType type, MysteryCell mysteryCell) {
        return planner.findOptions(pieces, roll, mysteryCell).stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no " + type + " option"));
    }

    void placeOnTrack(Piece piece, int cell) {
        board.enter(piece, Direction.CLOCKWISE);
        board.move(piece, Position.onTrack(cell));
    }
}
