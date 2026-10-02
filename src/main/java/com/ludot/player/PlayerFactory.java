package com.ludot.player;

import com.ludot.board.Colour;
import com.ludot.board.TrackNavigator;
import com.ludot.player.MoveRule.AvoidBlockClosestToHome;
import com.ludot.player.MoveRule.BlockMove;
import com.ludot.player.MoveRule.CaptureByPieceNeedingCapture;
import com.ludot.player.MoveRule.CaptureClosestToVictimHome;
import com.ludot.player.MoveRule.CaptureNeededWithoutBreakingBlock;
import com.ludot.player.MoveRule.ClosestToHome;
import com.ludot.player.MoveRule.EnterFromBase;
import com.ludot.player.MoveRule.EnterFromBaseWithoutFormingBlock;
import com.ludot.player.MoveRule.FirstAvailable;
import com.ludot.player.MoveRule.FormBlock;
import com.ludot.player.MoveRule.NonBlockPieceClosestToHome;

public final class PlayerFactory {

    private final TrackNavigator navigator;

    public PlayerFactory(TrackNavigator navigator) {
        this.navigator = navigator;
    }

    public Player createPlayer(Colour colour) {
        return new Player(colour, createStrategy(colour));
    }

    // Each colour's rules from Brief 2.1, in priority order
    private PlayerStrategy createStrategy(Colour colour) {
        return switch (colour) {
            case RED -> new CaptureClosestToVictimHome(navigator,
                    new EnterFromBaseWithoutFormingBlock(
                            new AvoidBlockClosestToHome(navigator,
                                    new FirstAvailable())));
            case GREEN -> new FormBlock(
                    new EnterFromBase(
                            new BlockMove(
                                    new CaptureNeededWithoutBreakingBlock(
                                            new NonBlockPieceClosestToHome(navigator,
                                                    new FirstAvailable())))));
            case YELLOW -> new EnterFromBase(
                    new CaptureByPieceNeedingCapture(
                            new ClosestToHome(navigator,
                                    new FirstAvailable())));
            case BLUE -> new CyclicMysteryStrategy();
        };
    }
}
