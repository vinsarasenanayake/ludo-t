package com.ludot.infrastructure;

import com.ludot.domain.ActiveMysteryCell;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.NoMysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.TeleportDestination;
import com.ludot.dto.PieceLocationDto;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.port.EffectNotice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageFormatterTest {

    private MessageFormatter messages;
    private Piece red1;
    private Piece green1;

    @BeforeEach
    void setUp() {
        messages = new MessageFormatter();
        red1 = new Piece(Colour.RED, 1);
        green1 = new Piece(Colour.GREEN, 1);
    }

    @Test
    @DisplayName("Brief 3.1: message before the game begins")
    void playerIntroduction() {
        assertEquals("The red player has four (04) pieces named R1, R2, R3, and R4.",
                messages.playerIntroduction(Colour.RED));
    }

    @Test
    void openingRoll() {
        assertEquals("Yellow rolls 6", messages.openingRoll(Colour.YELLOW, 6));
    }

    @Test
    @DisplayName("Brief 3.1: first player and round order")
    void turnOrder() {
        String expected = "Yellow player has the highest roll and will begin the game." + System.lineSeparator()
                + "The order of a single round is Yellow, Blue, Red, and Green.";
        assertEquals(expected, messages.turnOrder(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN)));
    }

    @Test
    void diceRolled() {
        assertEquals("Red player rolled 4.", messages.diceRolled(Colour.RED, 4));
    }

    @Test
    void pieceEntered() {
        assertEquals("Red player moves piece R1 to the starting point.", messages.pieceEntered(red1));
    }

    @Test
    @DisplayName("Assumption A17: the status line keeps the brief's exact wording")
    void playerStatus() {
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 1, 3, List.of());
        assertEquals("Red player now has 1/4 on pieces on the board and 3/4 pieces on the base.",
                messages.playerStatus(status));
    }

    @Test
    void pieceMoved() {
        red1.enterBoard(Direction.CLOCKWISE);
        red1.moveTo(Position.onTrack(30));
        assertEquals("Red moves piece R1 from location 26 to 30 by 4 units in clockwise direction.",
                messages.pieceMoved(red1, Position.onTrack(26), 4, Direction.CLOCKWISE));
    }

    @Test
    void pieceBlocked() {
        assertEquals("Red piece R1 is blocked from moving from 0 to 6 by Green piece G1.",
                messages.pieceBlocked(red1, Position.onTrack(0), Position.onTrack(6), green1));
    }

    @Test
    void capture() {
        assertEquals("Red piece R1 lands on square 30, captures Green piece G1, and returns it to the base.",
                messages.capture(red1, green1, Position.onTrack(30)));
    }

    @Test
    void teleportToAlpha() {
        String expected = "Red player lands on a mystery cell and is teleported to Alpha." + System.lineSeparator()
                + "Red piece R1 teleported to Alpha.";
        assertEquals(expected, messages.teleport(red1, TeleportDestination.ALPHA));
    }

    @Test
    void energisedEffect() {
        assertEquals("Red piece R1 feels energized, and movement speed doubles.",
                messages.effect(red1, EffectNotice.ENERGISED));
    }

    @Test
    void gammaReversesDirection() {
        assertEquals("The Red piece R1, which was moving clockwise, has changed to moving counterclockwise.",
                messages.effect(red1, EffectNotice.DIRECTION_REVERSED));
    }

    @Test
    void mysteryCellSpawned() {
        assertEquals("A mystery cell has spawned in location 17 and will be at this location for the next four rounds.",
                messages.mysteryCellSpawned(new ActiveMysteryCell(17, 4)));
    }

    @Test
    @DisplayName("Brief 3.1: after each round, piece locations and the mystery cell are shown")
    void roundSummaryListsPieceLocationsAndMysteryCell() {
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 1, 3,
                List.of(new PieceLocationDto("R1", "30"), new PieceLocationDto("R2", "Base")));
        String summary = messages.roundSummary(List.of(status), new ActiveMysteryCell(17, 3));
        assertTrue(summary.contains("Location of pieces Red"));
        assertTrue(summary.contains("Piece R1 -> 30"));
        assertTrue(summary.contains("The mystery cell is at 17 and will be at that location for the next 3 rounds."));
    }

    @Test
    void roundSummaryBeforeTheMysteryCellAppears() {
        String summary = messages.roundSummary(List.of(), NoMysteryCell.INSTANCE);
        assertEquals("There is no mystery cell on the board yet.", summary);
    }

    @Test
    @DisplayName("Brief 3.1: upon winning")
    void winner() {
        assertEquals("Red player wins!!!", messages.playerFinished(Colour.RED, 1));
    }

    @Test
    void laterPlaces() {
        assertEquals("Blue player finishes in place 2.", messages.playerFinished(Colour.BLUE, 2));
    }
}