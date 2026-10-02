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

// T-11 to T-14: rolls the destination and applies its effect
public final class Teleporter {

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
        Position location = locationOf(destination, piece);
        listener.onTeleport(piece, destination, location);
        // A plain move: no capture or block check on arrival
        switch (destination) {
            case ALPHA -> sendToAlpha(piece, location);
            case BETA -> sendToBeta(piece, location);
            case GAMMA -> sendToGamma(piece, location);
            case BASE -> board.sendToBase(piece);
            case START -> board.move(piece, location);
            case APPROACH -> sendToApproach(piece, location);
        }
    }

    private Position locationOf(Destination destination, Piece piece) {
        return switch (destination) {
            case ALPHA -> teleportCell(ALPHA_OFFSET);
            case BETA -> teleportCell(BETA_OFFSET);
            case GAMMA -> teleportCell(GAMMA_OFFSET);
            case BASE -> Position.base();
            case START -> Position.onTrack(piece.colour().startCell());
            case APPROACH -> Position.onTrack(piece.colour().approachCell());
        };
    }

    // T-12: heads energised, tails sick
    private void sendToAlpha(Piece piece, Position alpha) {
        board.move(piece, alpha);
        boolean energised = coin.tossHeads();
        piece.applyEffect(energised ? new PieceEffect.Energised() : new PieceEffect.Sick());
        listener.onEffectApplied(piece, energised ? EffectNotice.ENERGISED : EffectNotice.SICK);
    }

    private void sendToBeta(Piece piece, Position beta) {
        board.move(piece, beta);
        piece.applyEffect(new PieceEffect.Briefing());
        listener.onEffectApplied(piece, EffectNotice.BRIEFING);
    }

    // T-14: clockwise turns round; counter-clockwise goes on to Beta
    private void sendToGamma(Piece piece, Position gamma) {
        board.move(piece, gamma);
        if (piece.direction() == Direction.CLOCKWISE) {
            piece.reverseDirection();
            listener.onEffectApplied(piece, EffectNotice.DIRECTION_REVERSED);
            return;
        }
        listener.onEffectApplied(piece, EffectNotice.SENT_TO_BETA);
        sendToBeta(piece, teleportCell(BETA_OFFSET));
    }

    // R9: counts as passing the approach
    private void sendToApproach(Piece piece, Position approach) {
        board.move(piece, approach);
        piece.recordApproachPass();
    }

    // T-11: counted from Yellow's approach cell
    private Position teleportCell(int offsetFromYellowApproach) {
        int cell = navigator.move(Colour.YELLOW.approachCell(), offsetFromYellowApproach, Direction.CLOCKWISE);
        return Position.onTrack(cell);
    }
}
