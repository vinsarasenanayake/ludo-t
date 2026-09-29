package com.ludot.mystery;

import com.ludot.board.Piece;

public interface MysteryEvents {

    enum EffectNotice { ENERGISED, SICK, BRIEFING, DIRECTION_REVERSED, SENT_TO_BETA }

    void onMysteryCellSpawned(MysteryCell mysteryCell);

    void onTeleport(Piece piece, MysteryCell.Destination destination);

    void onEffectApplied(Piece piece, EffectNotice notice);
}
