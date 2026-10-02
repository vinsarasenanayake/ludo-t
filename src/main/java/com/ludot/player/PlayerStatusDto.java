package com.ludot.player;

import com.ludot.board.Colour;

import java.util.List;

// DTO: a snapshot of a player for printing
public record PlayerStatusDto(Colour colour, int piecesOnBoard, int piecesInBase, List<PieceLocation> pieces) {

    public record PieceLocation(String pieceName, String location) {
    }

    public PlayerStatusDto {
        pieces = List.copyOf(pieces);
    }
}