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
import com.ludot.port.MysteryListener;

import static com.ludot.domain.BoardConstants.ALPHA_OFFSET;
import static com.ludot.domain.BoardConstants.BETA_OFFSET;
import static com.ludot.domain.BoardConstants.GAMMA_OFFSET;

// Rules T-11 to T-15: effects only happen when a piece arrives here through a mystery cell.
public class Teleporter {

    private final Board board;
    private final Dice dice;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final EffectFactory effectFactory;
    private final MysteryListener listener;

    public Teleporter(Board board, Dice dice, Coin coin, TrackNavigator navigator,
                      EffectFactory effectFactory, MysteryListener listener) {
        this.board = board;
        this.dice = dice;
        this.coin = coin;
        this.navigator = navigator;
        this.effectFactory = effectFactory;
        this.listener = listener;
    }

    public void teleport(Piece piece) {
        // Rule T-11: "randomly select one of six options" is done with a die roll.
        TeleportDestination destination = TeleportDestination.fromDieFace(dice.roll());
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

    // Rule T-12: a coin toss decides energised (heads) or sick (tails).
    private void sendToAlpha(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(ALPHA_OFFSET)));
        boolean energised = coin.tossHeads();
        piece.applyEffect(energised ? effectFactory.createEnergisedEffect() : effectFactory.createSickEffect());
        listener.onEffectApplied(piece, energised ? EffectNotice.ENERGISED : EffectNotice.SICK);
    }

    private void sendToBeta(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(BETA_OFFSET)));
        piece.applyEffect(effectFactory.createBriefingEffect());
        listener.onEffectApplied(piece, EffectNotice.BRIEFING);
    }

    // Rule T-14: clockwise pieces turn around; counter-clockwise pieces are sent on to Beta.
    private void sendToGamma(Piece piece) {
        board.move(piece, Position.onTrack(teleportCell(GAMMA_OFFSET)));
        if (piece.direction() == Direction.CLOCKWISE) {
            piece.reverseDirection();
            listener.onEffectApplied(piece, EffectNotice.DIRECTION_REVERSED);
            return;
        }
        listener.onEffectApplied(piece, EffectNotice.SENT_TO_BETA);
        listener.onTeleport(piece, TeleportDestination.BETA);
        sendToBeta(piece);
    }

    // Assumption: landing on the approach this way counts as passing it once.
    private void sendToApproach(Piece piece) {
        board.move(piece, Position.onTrack(piece.colour().approachCell()));
        piece.recordApproachPass();
    }

    // Rule T-11: Alpha, Beta and Gamma are counted clockwise from the yellow approach cell (cell 0 of the count).
    private int teleportCell(int offsetFromYellowApproach) {
        return navigator.move(Colour.YELLOW.approachCell(), offsetFromYellowApproach, Direction.CLOCKWISE);
    }
}
