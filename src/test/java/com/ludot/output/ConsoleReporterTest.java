package com.ludot.output;

import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;
import com.ludot.game.GameResultDto;
import com.ludot.mystery.MysteryCell;
import com.ludot.mystery.MysteryEvents.EffectNotice;
import com.ludot.player.PlayerStatusDto;

import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        printed = new ByteArrayOutputStream();
        reporter = new ConsoleReporter(new PrintStream(printed, true));
    }

    // Messages before the game begins
    @Test
    void playerIntroductionAndTurnOrder() {
        reporter.onPlayerIntroduced(Colour.RED);
        reporter.onTurnOrderDecided(List.of(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN));
        assertPrinted("The red player has four (04) pieces named R1, R2, R3, and R4." + NEW_LINE
                + "Yellow player has the highest roll and will begin the game." + NEW_LINE
                + "The order of a single round is yellow, blue, red, and green.");
    }

    // Status line keeps the exact wording
    @Test
    void statusLineKeepsTheBriefsWording() {
        reporter.onPlayerStatus(new PlayerStatusDto(Colour.RED, 1, 3, List.of()));
        assertPrinted("Red player now has 1/4 on pieces on the board and 3/4 pieces on the base.");
    }

    // Round summary with locations and mystery cell
    @Test
    void roundSummaryListsPieceLocationsAndMysteryCell() {
        PlayerStatusDto status = new PlayerStatusDto(Colour.RED, 1, 3, List.of(
                new PlayerStatusDto.PieceLocation("R1", "30"), new PlayerStatusDto.PieceLocation("R2", "Base")));
        reporter.onRoundEnded(List.of(status), new MysteryCell.Active(17, 3));
        String summary = printed.toString();
        assertTrue(summary.contains("Location of pieces red"));
        assertTrue(summary.contains("Piece R1 -> 30"));
        assertTrue(summary.contains("Piece R2 -> Base"));
        assertTrue(summary.contains("The mystery cell is at 17 and will be at that location for the next 3 values."));
    }

    // R11: winner first, then the final ranking
    @Test
    void onlyTheWinnerIsAnnouncedThenFinalRanking() {
        reporter.onPlayerFinished(Colour.RED, 1);
        reporter.onPlayerFinished(Colour.BLUE, 2);
        List<Colour> ranking = List.of(Colour.RED, Colour.BLUE, Colour.GREEN, Colour.YELLOW);
        reporter.onGameOver(new GameResultDto(ranking, 90));
        assertPrinted("Red player wins!!!" + NEW_LINE
                + "============================" + NEW_LINE + "Final results after 90 rounds" + NEW_LINE
                + "============================" + NEW_LINE + "1st place: Red" + NEW_LINE
                + "2nd place: Blue" + NEW_LINE + "3rd place: Green" + NEW_LINE + "4th place: Yellow");
    }

    // R2: dice roll and entering messages
    @Test
    void diceRollAndPieceEntering() {
        reporter.onDiceRolled(Colour.RED, 5);
        reporter.onPieceEntered(new Piece(Colour.RED, 1));
        assertPrinted("Red player rolled 5." + NEW_LINE + "Red player moves piece R1 to the starting point.");
    }

    // R1 + R6: move and capture messages
    @Test
    void pieceMovedAndCapture() {
        Piece red1 = new Piece(Colour.RED, 1);
        Route route = Route.completed(Position.onTrack(26), Position.onTrack(30), 4, 0);
        reporter.onPieceMoved(red1, route, Direction.CLOCKWISE);
        reporter.onCapture(red1, new Piece(Colour.GREEN, 1), Position.onTrack(30));
        assertPrinted("Red moves piece R1 from location 26 to 30 by 4 units in clockwise direction." + NEW_LINE
                + "Red piece R1 lands on square 30, captures green piece G1, and returns it to the base.");
    }

    // T-3: the three blocked messages
    @Test
    void blockedMoveMessages() {
        Piece red1 = new Piece(Colour.RED, 1);
        Route.Blockage blockage = new Route.Blockage(Position.onTrack(30), new Piece(Colour.GREEN, 1));
        reporter.onPieceBlocked(red1, Position.onTrack(26), blockage);
        reporter.onMovedBeforeBlock(Colour.RED, Position.onTrack(29));
        reporter.onBlockedThrowIgnored(Colour.RED);
        assertPrinted("Red piece R1 is blocked from moving from 26 to 30 by green piece G1." + NEW_LINE
                + "Red does not have other pieces in the board to move instead of the blocked piece. "
                + "Moved the piece to square 29 which is the cell before the block." + NEW_LINE
                + "Red does not have other pieces in the board to move instead of the blocked piece. "
                + "Ignoring the throw and moving on to the next player.");
    }

    // T-10 + T-11 + T-12: spawn, teleport, and sick messages
    @Test
    void mysteryCellTeleportAndEffect() {
        Piece red1 = new Piece(Colour.RED, 1);
        reporter.onMysteryCellSpawned(new MysteryCell.Active(10, 4));
        reporter.onTeleport(red1, MysteryCell.Destination.ALPHA, Position.onTrack(7));
        reporter.onEffectApplied(red1, EffectNotice.SICK);
        assertPrinted("A mystery cell has spawned in location 10 and will be at this location for the next four rounds."
                + NEW_LINE + "Red player lands on a mystery cell and is teleported to 7." + NEW_LINE
                + "Red piece R1 teleported to Alpha." + NEW_LINE
                + "Red piece R1 feels sick, and movement speed halves.");
    }

    // T-12 + T-13 + T-14: the other effect messages
    @Test
    void otherEffectMessages() {
        Piece blue2 = new Piece(Colour.BLUE, 2);
        reporter.onEffectApplied(blue2, EffectNotice.ENERGISED);
        reporter.onEffectApplied(blue2, EffectNotice.BRIEFING);
        reporter.onEffectApplied(blue2, EffectNotice.DIRECTION_REVERSED);
        reporter.onEffectApplied(blue2, EffectNotice.SENT_TO_BETA);
        reporter.onSentBackFromBriefing(blue2);
        assertPrinted("Blue piece B2 feels energized, and movement speed doubles." + NEW_LINE
                + "Blue piece B2 attends briefing and cannot move for four rounds." + NEW_LINE
                + "The blue piece B2, which was moving clockwise, has changed to moving counterclockwise." + NEW_LINE
                + "The blue piece B2 is moving in a counterclockwise direction. "
                + "Teleporting to Beta from Gamma." + NEW_LINE
                + "Blue piece B2 is movement-restricted and has rolled three consecutively. "
                + "Teleporting piece B2 to base.");
    }

    // Opening roll message
    @Test
    void openingRoll() {
        reporter.onOpeningRoll(Colour.GREEN, 4);
        assertPrinted("Green rolls 4");
    }

    // T-11: teleport to base names Base
    @Test
    void teleportToBaseNamesBase() {
        reporter.onTeleport(new Piece(Colour.BLUE, 3), MysteryCell.Destination.BASE, Position.base());
        assertPrinted("Blue player lands on a mystery cell and is teleported to Base." + NEW_LINE
                + "Blue piece B3 teleported to Base.");
    }

    private void assertPrinted(String expected) {
        assertEquals(expected + NEW_LINE, printed.toString());
    }
}