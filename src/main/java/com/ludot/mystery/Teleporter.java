package com.ludot.mystery;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.PieceEffect;
import com.ludot.board.Position;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCell.Destination;
import com.ludot.mystery.MysteryEvents.EffectNotice;
import com.ludot.random.Coin;
import com.ludot.random.Dice;

import static com.ludot.board.BoardConstants.ALPHA_OFFSET;
import static com.ludot.board.BoardConstants.BETA_OFFSET;
import static com.ludot.board.BoardConstants.GAMMA_OFFSET;

public class Teleporter {

    private final Board board;
    private final Dice dice;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final MysteryEvents listener;

    public Teleporter(Board board, Dice dice, Coin coin, TrackNavigator navigator, MysteryEvents listener) {
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
