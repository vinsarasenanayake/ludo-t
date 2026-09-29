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
        List<PlayerStatusDto.PieceLocation> pieces = new ArrayList<>(List.of(new PlayerStatusDto.PieceLocation("R1", "Base")));
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 0, 4, pieces);
        pieces.add(new PlayerStatusDto.PieceLocation("R2", "Base"));
        assertEquals(1, status.pieces().size());
    }

    @Test
    void playerStatusPiecesCannotBeModified() {
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 0, 4, List.of());
        assertThrows(UnsupportedOperationException.class,
                () -> status.pieces().add(new PlayerStatusDto.PieceLocation("R1", "Base")));
    }

    @Test
    void gameResultKeepsItsOwnCopyOfTheFinishingOrder() {
        List<Colour> order = new ArrayList<>(List.of(Colour.BLUE, Colour.RED));
        GameResultDto result = new GameResultDto(order, 120, false);
        order.add(Colour.GREEN);
        assertEquals(List.of(Colour.BLUE, Colour.RED), result.finishingOrder());
    }
}
