package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

import java.util.Comparator;
import java.util.Optional;

public class AvoidBlockClosestToHomeSelector extends ChainedMoveSelector {

    private final TrackNavigator navigator;

    public AvoidBlockClosestToHomeSelector(TrackNavigator navigator, MoveSelector next) {
        super(next);
        this.navigator = navigator;
    }

    @Override
    protected Optional<MoveOption> trySelect(TurnContext context) {
        return context.options().stream()
                .filter(option -> !option.formsBlock())
                .min(Comparator.comparingInt(option -> navigator.stepsToHome(option.leadPiece())));
    }
}