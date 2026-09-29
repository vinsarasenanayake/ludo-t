package com.ludot.engine;

import com.ludot.domain.Piece;
import com.ludot.dto.PieceLocationDto;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.player.Player;

import java.util.List;

public class StatusSnapshotFactory {

    public PlayerStatusDto snapshotOf(Player player) {
        List<PieceLocationDto> locations = player.pieces().stream()
                .map(this::locationOf)
                .toList();
        return new PlayerStatusDto(player.colour(), player.piecesOnBoard(), player.piecesInBase(), locations);
    }

    private PieceLocationDto locationOf(Piece piece) {
        return new PieceLocationDto(piece.name(), piece.position().describe(piece.colour()));
    }
}
