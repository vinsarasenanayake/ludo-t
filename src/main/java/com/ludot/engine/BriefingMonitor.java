package com.ludot.engine;

import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.player.Player;
import com.ludot.port.EffectNotice;
import com.ludot.port.MysteryListener;

// Rule T-13: a piece in a briefing goes back to base if its player rolls a three twice in a row.
public class BriefingMonitor {

    private final Board board;
    private final MysteryListener listener;

    public BriefingMonitor(Board board, MysteryListener listener) {
        this.board = board;
        this.listener = listener;
    }

    public void observeRoll(Player player, int roll) {
        for (Piece piece : player.pieces()) {
            piece.observeRoll(roll);
            if (piece.requiresReturnToBase()) {
                listener.onEffectApplied(piece, EffectNotice.BRIEFING_RETURN_TO_BASE);
                board.sendToBase(piece);
            }
        }
    }
}
