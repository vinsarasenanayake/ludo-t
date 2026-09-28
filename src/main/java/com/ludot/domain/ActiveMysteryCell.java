package com.ludot.domain;

import static com.ludot.domain.BoardConstants.TRACK_SIZE;

public record ActiveMysteryCell(int location, int roundsRemaining) implements MysteryCell {

    public ActiveMysteryCell {
        if (location < 0 || location >= TRACK_SIZE) {
            throw new IllegalArgumentException("Mystery cell must be on the track but was " + location);
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isAt(int cell) {
        return location == cell;
    }

    public ActiveMysteryCell afterOneRound() {
        return new ActiveMysteryCell(location, roundsRemaining - 1);
    }
}