package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveType;
import com.ludot.rules.TrackNavigator;

import java.util.Comparator;
import java.util.Optional;

public class NonBlockPieceClosestToHomeSelector extends ChainedMoveSelector {

    private final TrackNavigator navigator;

    public NonBlockPieceClosestToHomeSelector(TrackNavigator navigator, MoveSelector next) {
        super(next);
        this.navigator = navigator;
    }

    @Override
    protected Optional<MoveOption> trySelect(TurnContext context) {
        return context.options().stream()
                .filter(option -> option.type() == MoveType.MOVE_PIECE)
                .filter(option -> !option.leavesBlock())
                .min(Comparator.comparingInt(option -> navigator.stepsToHome(option.leadPiece())));
    }
}