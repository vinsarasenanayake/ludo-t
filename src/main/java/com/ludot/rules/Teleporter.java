package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell.Destination;
import com.ludot.domain.Piece;
import com.ludot.domain.PieceEffect;
import com.ludot.domain.Position;
import com.ludot.port.Coin;
import com.ludot.port.Dice;
import com.ludot.port.GameEvents;
import com.ludot.port.GameEvents.EffectNotice;

import static com.ludot.domain.BoardConstants.ALPHA_OFFSET;
import static com.ludot.domain.BoardConstants.BETA_OFFSET;
import static com.ludot.domain.BoardConstants.GAMMA_OFFSET;

public class Teleporter {

    private final Board board;
    private final Dice dice;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final GameEvents.Mystery listener;

    public Teleporter(Board board, Dice dice, Coin coin, TrackNavigator navigator, GameEvents.Mystery listener) {
        this.board = board;
        this.dice = dice;
        this.coin = coin;
        this.navigator = navigator;
        this.listener = listener;
    }

    public void teleport(Piece piece) {
        Destination destination = Destination.fromDieFace(dice.roll());
        listener.onTeleport(piece, destination);
        switch (destination) {
            case ALPHA -> sendToAlpha(piece);
            case BETA -> sendToBeta(piece);
            case GAMMA -> sendToGamma(piece);
            case BASE -> board.sendToBase(piece);
            case START -> board.move(piece, Position.onTrack(piece.colour().startCell()));
            case APPROACH -> sendToApproach(piece);
        }
    }

    private void sendToAlpha(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(ALPHA_OFFSET)));
        boolean energised = coin.tossHeads();
        piece.applyEffect(energised ? new PieceEffect.Energised() : new PieceEffect.Sick());
        listener.onEffectApplied(piece, energised ? EffectNotice.ENERGISED : EffectNotice.SICK);
    }

    private void sendToBeta(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(BETA_OFFSET)));
        piece.applyEffect(new PieceEffect.Briefing());
        listener.onEffectApplied(piece, EffectNotice.BRIEFING);
    }

    private void sendToGamma(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(GAMMA_OFFSET)));
        if (piece.direction() == Direction.CLOCKWISE) {
            piece.reverseDirection();
            listener.onEffectApplied(piece, EffectNotice.DIRECTION_REVERSED);
            return;
        }
        listener.onEffectApplied(piece, EffectNotice.SENT_TO_BETA);
        listener.onTeleport(piece, Destination.BETA);
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
