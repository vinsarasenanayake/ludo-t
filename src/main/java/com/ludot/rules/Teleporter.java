package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.TeleportDestination;
import com.ludot.port.Coin;
import com.ludot.port.Dice;
import com.ludot.port.EffectNotice;
import com.ludot.port.GameObserver;

import static com.ludot.domain.BoardConstants.ALPHA_OFFSET;
import static com.ludot.domain.BoardConstants.BETA_OFFSET;
import static com.ludot.domain.BoardConstants.GAMMA_OFFSET;

public class Teleporter {

    private final Board board;
    private final Dice dice;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final EffectFactory effectFactory;
    private final GameObserver observer;

    public Teleporter(Board board, Dice dice, Coin coin, TrackNavigator navigator,
                      EffectFactory effectFactory, GameObserver observer) {
        this.board = board;
        this.dice = dice;
        this.coin = coin;
        this.navigator = navigator;
        this.effectFactory = effectFactory;
        this.observer = observer;
    }

    public TeleportDestination teleport(Piece piece) {
        TeleportDestination destination = TeleportDestination.fromDieFace(dice.roll());
        observer.onTeleport(piece, destination);
        switch (destination) {
            case ALPHA -> sendToAlpha(piece);
            case BETA -> sendToBeta(piece);
            case GAMMA -> sendToGamma(piece);
            case BASE -> board.sendToBase(piece);
            case START -> board.move(piece, Position.onTrack(piece.colour().startCell()));
            case APPROACH -> sendToApproach(piece);
        }
        return destination;
    }

    private void sendToAlpha(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(ALPHA_OFFSET)));
        boolean energised = coin.tossHeads();
        piece.applyEffect(energised ? effectFactory.createEnergisedEffect() : effectFactory.createSickEffect());
        observer.onEffectApplied(piece, energised ? EffectNotice.ENERGISED : EffectNotice.SICK);
    }

    private void sendToBeta(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(BETA_OFFSET)));
        piece.applyEffect(effectFactory.createBriefingEffect());
        observer.onEffectApplied(piece, EffectNotice.BRIEFING);
    }

    private void sendToGamma(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(GAMMA_OFFSET)));
        if (piece.direction() == Direction.CLOCKWISE) {
            piece.reverseDirection();
            observer.onEffectApplied(piece, EffectNotice.DIRECTION_REVERSED);
            return;
        }
        observer.onEffectApplied(piece, EffectNotice.SENT_TO_BETA);
        observer.onTeleport(piece, TeleportDestination.BETA);
        sendToBeta(piece);
    }

    private void sendToApproach(Piece piece) {
        board.move(piece, Position.onTrack(piece.colour().approachCell()));
        piece.recordApproachPass();
    }

    private int teleportCell(int offsetFromYellowApproach) {
        return navigator.move(Colour.YELLOW.approachCell(), offsetFromYellowApproach, Direction.CLOCKWISE);
    }
}