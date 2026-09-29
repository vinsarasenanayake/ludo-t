package com.ludot.engine;

import com.ludot.domain.Colour;

import java.util.List;

public record GameOutcome(List<Colour> finishingOrder, int rounds, boolean stalled) {

    public GameOutcome {
        finishingOrder = List.copyOf(finishingOrder);
    }
}