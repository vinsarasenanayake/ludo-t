package com.ludot.player;

import com.ludot.player.selector.BlockMoveSelector;
import com.ludot.player.selector.EnterFromBaseSelector;
import com.ludot.player.selector.FirstAvailableSelector;
import com.ludot.player.selector.FormBlockSelector;
import com.ludot.player.selector.MoveSelector;
import com.ludot.player.selector.NonBlockPieceClosestToHomeSelector;
import com.ludot.player.selector.TurnContext;
import com.ludot.rules.MoveOption;
import com.ludot.rules.TrackNavigator;

// Green (section 2.1.2): form blocks, keep the base empty, move pieces outside the block, then move the block.
public class BlockingStrategy implements PlayerStrategy {

    private final MoveSelector chain;

    public BlockingStrategy(TrackNavigator navigator) {
        this.chain = new FormBlockSelector(
                new EnterFromBaseSelector(
                        new NonBlockPieceClosestToHomeSelector(navigator,
                                new BlockMoveSelector(
                                        new FirstAvailableSelector()))));
    }

    @Override
    public MoveOption chooseMove(TurnContext context) {
        return chain.select(context);
    }
}
