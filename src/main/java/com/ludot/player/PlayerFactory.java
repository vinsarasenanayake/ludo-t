package com.ludot.player;

import com.ludot.board.Colour;
import com.ludot.board.TrackNavigator;
import com.ludot.player.MoveRule.AvoidBlockClosestToHome;
import com.ludot.player.MoveRule.BlockMove;
import com.ludot.player.MoveRule.CaptureByPieceNeedingCapture;
import com.ludot.player.MoveRule.CaptureClosestToVictimHome;
import com.ludot.player.MoveRule.ClosestToHome;
import com.ludot.player.MoveRule.EnterFromBase;
import com.ludot.player.MoveRule.FirstAvailable;
import com.ludot.player.MoveRule.FormBlock;
import com.ludot.player.MoveRule.NonBlockPieceClosestToHome;

public class PlayerFactory {

    private final TrackNavigator navigator;

    public PlayerFactory(TrackNavigator navigator) {
        this.navigator = navigator;
    }

    public Player createPlayer(Colour colour) {
        return new Player(colour, createStrategy(colour));
    }

    private PlayerStrategy createStrategy(Colour colour) {
        return switch (colour) {
            case RED -> new CaptureClosestToVictimHome(navigator,
                    new EnterFromBase(
                            new AvoidBlockClosestToHome(navigator,
                                    new FirstAvailable())));
            case GREEN -> new FormBlock(
                    new EnterFromBase(
                            new NonBlockPieceClosestToHome(navigator,
                                    new BlockMove(
                                            new FirstAvailable()))));
            case YELLOW -> new EnterFromBase(
                    new CaptureByPieceNeedingCapture(
                            new ClosestToHome(navigator,
                                    new FirstAvailable())));
            case BLUE -> new CyclicMysteryStrategy();
        };
    }
}
