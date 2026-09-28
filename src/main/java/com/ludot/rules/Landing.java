package com.ludot.rules;

import com.ludot.domain.Piece;

import java.util.List;

public record Landing(List<Piece> victims, boolean formsBlock, boolean onMysteryCell) {

    public static Landing offTrack() {
        return new Landing(List.of(), false, false);
    }

    public boolean capturesAny() {
        return !victims.isEmpty();
    }
}