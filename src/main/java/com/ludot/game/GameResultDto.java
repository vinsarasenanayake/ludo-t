package com.ludot.game;

import com.ludot.board.Colour;

import java.util.List;

public record GameResultDto(List<Colour> finishingOrder, int rounds) {

    public GameResultDto {
        finishingOrder = List.copyOf(finishingOrder);
    }
}