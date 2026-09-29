package com.ludot.infrastructure;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.Route;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.port.GameEvents.EffectNotice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleReporterTest {

    private static final String NEW_LINE = System.lineSeparator();

    private ByteArrayOutputStream printed;
    private ConsoleReporter reporter;
    private Piece red1;
    private Piece green1;

    @BeforeEach
    void setUp() {
        printed = new ByteArrayOutputStream();
        reporter = new ConsoleReporter(new PrintStream(printed, true));
        red1 = new Piece(Colour.RED, 1);
        green1 = new Piece(Colour.GREEN, 1);
    }

    @Test
    @DisplayName("Brief 3.1: message before the game begins")
    void playerIntroduction() {
        reporter.onPlayerIntroduced(Colour.RED);
        assertPrinted("The red player has four (04) pieces named R1, R2, R3, and R4.");
    }

    @Test
    void openingRoll() {
        reporter.onOpeningRoll(Colour.YELLOW, 6);
        assertPrinted("Yellow rolls 6");
    }

    @Test
    @DisplayName("Brief 3.1: first player and round order")
    void turnOrder() {
        reporter.onTurnOrderDecided(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN));
        assertPrinted("Yellow player has the highest roll and will begin the game." + NEW_LINE
                + "The order of a single round is Yellow, Blue, Red, and Green.");
    }

    @Test
    void diceRolled() {
        reporter.onDiceRolled(Colour.RED, 4);
        assertPrinted("Red player rolled 4.");
    }

    @Test
    void pieceEntered() {
        reporter.onPieceEntered(new Piece(Colour.GREEN, 2));
        assertPrinted("Green player moves piece G2 to the starting point.");
    }

    @Test
    @DisplayName("Assumption A17: the status line keeps the brief's exact wording")
    void playerStatus() {
        reporter.onPlayerStatus(new PlayerStatusDto(Colour.RED, 1, 3, List.of()));
        assertPrinted("Red player now has 1/4 on pieces on the board and 3/4 pieces on the base.");
    }

    @Test
    void pieceMoved() {
        red1.enterBoard(Direction.CLOCKWISE);
        red1.moveTo(Position.onTrack(30));
        reporter.onPieceMoved(red1, Route.completed(Position.onTrack(26), Position.onTrack(30), 4, 0), Direction.CLOCKWISE);
        assertPrinted("Red moves piece R1 from location 26 to 30 by 4 units in clockwise direction.");
    }

    @Test
    void pieceBlocked() {
        reporter.onPieceBlocked(red1, Position.onTrack(0), new Route.Blockage(Position.onTrack(6), green1));
        assertPrinted("Red piece R1 is blocked from moving from 0 to 6 by Green piece G1.");
    }

    @Test
    void capture() {
        reporter.onCapture(red1, green1, Position.onTrack(30));
        assertPrinted("Red piece R1 lands on square 30, captures Green piece G1, and returns it to the base.");
    }

    @Test
    void teleportToAlpha() {
        reporter.onTeleport(red1, MysteryCell.Destination.ALPHA);
        assertPrinted("Red player lands on a mystery cell and is teleported to Alpha." + NEW_LINE
                + "Red piece R1 teleported to Alpha.");
    }

    @Test
    void energisedEffect() {
        reporter.onEffectApplied(red1, EffectNotice.ENERGISED);
        assertPrinted("Red piece R1 feels energized, and movement speed doubles.");
    }

    @Test
    void gammaReversesDirection() {
        reporter.onEffectApplied(red1, EffectNotice.DIRECTION_REVERSED);
        assertPrinted("The Red piece R1, which was moving clockwise, has changed to moving counterclockwise.");
    }

    @Test
    void briefingReturnToBase() {
        reporter.onSentBackFromBriefing(red1);
        assertPrinted("Red piece R1 is movement-restricted and has rolled three consecutively. Teleporting piece R1 to base.");
    }

    @Test
    void mysteryCellSpawned() {
        reporter.onMysteryCellSpawned(new MysteryCell.Active(17, 4));
        assertPrinted("A mystery cell has spawned in location 17 and will be at this location for the next four rounds.");
    }

    @Test
    @DisplayName("Brief 3.1: after each round, piece locations and the mystery cell are shown")
    void roundSummaryListsPieceLocationsAndMysteryCell() {
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 1, 3, List.of(
                new PlayerStatusDto.PieceLocation("R1", "30"), new PlayerStatusDto.PieceLocation("R2", "Base")));
        reporter.onRoundEnded(List.of(status), new MysteryCell.Active(17, 3));
        String summary = printed.toString();
        assertTrue(summary.contains("Location of pieces Red"));
        assertTrue(summary.contains("Piece R1 -> 30"));
        assertTrue(summary.contains("The mystery cell is at 17 and will be at that location for the next 3 rounds."));
    }

    @Test
    void roundSummaryBeforeTheMysteryCellAppears() {
        reporter.onRoundEnded(List.of(), MysteryCell.None.INSTANCE);
        assertEquals("There is no mystery cell on the board yet." + NEW_LINE + NEW_LINE, printed.toString());
    }

    @Test
    @DisplayName("Brief 3.1: upon winning")
    void winner() {
        reporter.onPlayerFinished(Colour.RED, 1);
        assertPrinted("Red player wins!!!");
    }

    @Test
    void laterPlaces() {
        reporter.onPlayerFinished(Colour.BLUE, 2);
        assertPrinted("Blue player finishes in place 2.");
    }

    @Test
    @DisplayName("A six-sided die only rolls 1 to 6")
    void diceRollsStayBetweenOneAndSix() {
        SeededRandomness random = new SeededRandomness(7);
        for (int roll = 0; roll < 1000; roll++) {
            int value = random.roll();
            assertTrue(value >= 1 && value <= 6);
        }
    }

    @Test
    @DisplayName("The same seed gives the same rolls, which makes demos repeatable")
    void sameSeedGivesSameRolls() {
        SeededRandomness first = new SeededRandomness(42);
        SeededRandomness second = new SeededRandomness(42);
        for (int roll = 0; roll < 20; roll++) {
            assertEquals(first.roll(), second.roll());
        }
    }

    @Test
    void coinLandsOnBothSides() {
        SeededRandomness random = new SeededRandomness(3);
        boolean sawHeads = false;
        boolean sawTails = false;
        for (int toss = 0; toss < 100; toss++) {
            boolean heads = random.tossHeads();
            sawHeads |= heads;
            sawTails |= !heads;
        }
        assertTrue(sawHeads && sawTails);
    }

    @Test
    void cellPickerOnlyPicksAllowedCells() {
        SeededRandomness random = new SeededRandomness(5);
        List<Integer> allowed = List.of(3, 17, 40);
        for (int pick = 0; pick < 50; pick++) {
            assertTrue(allowed.contains(random.pick(allowed)));
        }
    }

    private void assertPrinted(String expected) {
        assertEquals(expected + NEW_LINE, printed.toString());
    }
}
