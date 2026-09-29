package com.ludot.dto;

import com.ludot.domain.Colour;

import java.util.List;

public record GameResultDto(List<Colour> finishingOrder, int rounds, boolean stalled) {

    public GameResultDto {
        finishingOrder = List.copyOf(finishingOrder);
    }
}
