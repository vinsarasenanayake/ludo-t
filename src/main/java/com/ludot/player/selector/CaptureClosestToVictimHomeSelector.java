package com.ludot.player.selector;

import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

import java.util.Comparator;
import java.util.Optional;

public class CaptureClosestToVictimHomeSelector extends ChainedMoveSelector {

    private final TrackNavigator navigator;

    public CaptureClosestToVictimHomeSelector(TrackNavigator navigator, MoveSelector next) {
        super(next);
        this.navigator = navigator;
    }

    @Override
    protected Optional<MoveOption> trySelect(TurnContext context) {
        return context.options().stream()
                .filter(MoveOption::capturesAny)
                .min(Comparator.comparingInt(this::closestVictimStepsToHome));
    }

    private int closestVictimStepsToHome(MoveOption option) {
        return option.landing().victims().stream()
                .mapToInt(navigator::stepsToHome)
                .min()
                .orElse(TrackNavigator.NOT_ON_BOARD);
    }
}