package com.ludot.mystery;

import com.ludot.board.Piece;
import com.ludot.board.Position;

// Mystery cell events for the reporter
public interface MysteryEvents {

    enum EffectNotice { ENERGISED, SICK, BRIEFING, DIRECTION_REVERSED, SENT_TO_BETA }

    void onMysteryCellSpawned(MysteryCell mysteryCell);

    void onTeleport(Piece piece, MysteryCell.Destination destination, Position location);

    void onEffectApplied(Piece piece, EffectNotice notice);
}