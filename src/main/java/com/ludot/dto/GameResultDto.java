package com.ludot.dto;

import com.ludot.domain.Colour;

import java.util.List;

public record GameResultDto(long seed, int rounds, List<Colour> finishingOrder) {

    public GameResultDto {
        finishingOrder = List.copyOf(finishingOrder);
    }

    public Colour winner() {
        return finishingOrder.get(0);
    }
}
