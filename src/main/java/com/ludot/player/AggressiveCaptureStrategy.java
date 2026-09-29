package com.ludot.player;

import com.ludot.player.selector.AvoidBlockClosestToHomeSelector;
import com.ludot.player.selector.CaptureClosestToVictimHomeSelector;
import com.ludot.player.selector.EnterFromBaseSelector;
import com.ludot.player.selector.FirstAvailableSelector;
import com.ludot.player.selector.MoveSelector;
import com.ludot.player.selector.TurnContext;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

// Red (section 2.1.1): capture first, then enter from base, then avoid forming blocks.
public class AggressiveCaptureStrategy implements PlayerStrategy {

    private final MoveSelector chain;

    public AggressiveCaptureStrategy(TrackNavigator navigator) {
        this.chain = new CaptureClosestToVictimHomeSelector(navigator,
                new EnterFromBaseSelector(
                        new AvoidBlockClosestToHomeSelector(navigator,
                                new FirstAvailableSelector())));
    }

    @Override
    public MoveOption chooseMove(TurnContext context) {
        return chain.select(context);
    }
}
