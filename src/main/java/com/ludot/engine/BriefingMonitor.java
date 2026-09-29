package com.ludot.engine;

import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.player.Player;
import com.ludot.port.EffectNotice;
import com.ludot.port.GameObserver;

public class BriefingMonitor {

    private final Board board;
    private final GameObserver observer;

    public BriefingMonitor(Board board, GameObserver observer) {
        this.board = board;
        this.observer = observer;
    }

    public void observeRoll(Player player, int roll) {
        for (Piece piece : player.pieces()) {
            piece.observeRoll(roll);
            if (piece.requiresReturnToBase()) {
                observer.onEffectApplied(piece, EffectNotice.BRIEFING_RETURN_TO_BASE);
                board.sendToBase(piece);
            }
        }
    }
}