package com.ludot.game;

import com.ludot.board.Colour;

import java.util.List;

// DTO: the final ranking and the number of rounds
public record GameResultDto(List<Colour> finishingOrder, int rounds) {

    public GameResultDto {
        // Copied so the result cannot change later
        finishingOrder = List.copyOf(finishingOrder);
    }
}