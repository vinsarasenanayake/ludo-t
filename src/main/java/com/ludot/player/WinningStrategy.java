package com.ludot.player;

import com.ludot.player.selector.CaptureByPieceNeedingCaptureSelector;
import com.ludot.player.selector.ClosestToHomeSelector;
import com.ludot.player.selector.EnterFromBaseSelector;
import com.ludot.player.selector.FirstAvailableSelector;
import com.ludot.player.selector.MoveSelector;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

public class WinningStrategy implements PlayerStrategy {

    private final MoveSelector chain;

    public WinningStrategy(TrackNavigator navigator) {
        this.chain = new EnterFromBaseSelector(
                new CaptureByPieceNeedingCaptureSelector(
                        new ClosestToHomeSelector(navigator,
                                new FirstAvailableSelector())));
    }

    @Override
    public MoveOption chooseMove(TurnContext context) {
        return chain.select(context);
    }
}