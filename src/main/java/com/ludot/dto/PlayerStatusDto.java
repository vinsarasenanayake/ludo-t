package com.ludot.dto;

import com.ludot.domain.Colour;

import java.util.List;

public record PlayerStatusDto(Colour colour, int piecesOnBoard, int piecesInBase, List<PieceLocationDto> pieces) {

    public PlayerStatusDto {
        pieces = List.copyOf(pieces);
    }
}
