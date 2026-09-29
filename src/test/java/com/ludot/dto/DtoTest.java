package com.ludot.dto;

import com.ludot.domain.Colour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DtoTest {

    @Test
    @DisplayName("DTOs are immutable: changing the original list does not change the DTO")
    void playerStatusKeepsItsOwnCopyOfThePieces() {
        List<PieceLocationDto> pieces = new ArrayList<>(List.of(new PieceLocationDto("R1", "Base")));
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 0, 4, pieces);

        pieces.add(new PieceLocationDto("R2", "Base"));

        assertEquals(1, status.pieces().size());
    }

    @Test
    void playerStatusPiecesCannotBeModified() {
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 0, 4, List.of());
        assertThrows(UnsupportedOperationException.class,
                () -> status.pieces().add(new PieceLocationDto("R1", "Base")));
    }

    @Test
    void winnerIsTheFirstPlayerToFinish() {
        GameResultDto result = new GameResultDto(42L, 120, List.of(Colour.BLUE, Colour.RED, Colour.GREEN));
        assertEquals(Colour.BLUE, result.winner());
    }
}