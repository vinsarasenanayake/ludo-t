package com.ludot.infrastructure;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.TeleportDestination;
import com.ludot.dto.PieceLocationDto;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.port.EffectNotice;

import java.util.List;

import static com.ludot.domain.BoardConstants.PIECES_PER_PLAYER;

public class MessageFormatter {

    private static final String SEPARATOR = "============================";
    private static final int WINNING_PLACE = 1;

    public String playerIntroduction(Colour colour) {
        return String.format("The %s player has four (04) pieces named %s1, %s2, %s3, and %s4.",
                colour.displayName(), colour.initial(), colour.initial(), colour.initial(), colour.initial());
    }

    public String openingRoll(Colour colour, int value) {
        return String.format("%s rolls %d", title(colour), value);
    }

    public String turnOrder(List<Colour> order) {
        List<String> names = order.stream().map(this::title).toList();
        String allButLast = String.join(", ", names.subList(0, names.size() - 1));
        return String.format("%s player has the highest roll and will begin the game.%n"
                + "The order of a single round is %s, and %s.", names.get(0), allButLast, names.get(names.size() - 1));
    }

    public String diceRolled(Colour colour, int value) {
        return String.format("%s player rolled %d.", title(colour), value);
    }

    public String rollIgnored(Colour colour) {
        return String.format("%s rolled six three times in a row. The roll is ignored and the dice passes "
                + "to the next player.", title(colour));
    }

    public String pieceEntered(Piece piece) {
        return String.format("%s player moves piece %s to the starting point.", title(piece.colour()), piece.name());
    }

    public String playerStatus(PlayerStatusDto status) {
        return String.format("%s player now has %d/%d on pieces on the board and %d/%d pieces on the base.",
                title(status.colour()), status.piecesOnBoard(), PIECES_PER_PLAYER,
                status.piecesInBase(), PIECES_PER_PLAYER);
    }

    public String pieceMoved(Piece piece, Position from, int distance, Direction direction) {
        return String.format("%s moves piece %s from location %s to %s by %d units in %s direction.",
                title(piece.colour()), piece.name(), from.describe(piece.colour()),
                piece.position().describe(piece.colour()), distance, direction.displayName());
    }

    public String pieceBlocked(Piece piece, Position from, Position intended, Piece blockingPiece) {
        return String.format("%s piece %s is blocked from moving from %s to %s by %s piece %s.",
                title(piece.colour()), piece.name(), from.describe(piece.colour()),
                intended.describe(piece.colour()), title(blockingPiece.colour()), blockingPiece.name());
    }

    public String movedBeforeBlock(Colour colour, Position stoppedAt) {
        return String.format("%s does not have other pieces in the board to move instead of the blocked piece. "
                        + "Moved the piece to square %s which is the cell before the block.",
                title(colour), stoppedAt.describe(colour));
    }

    public String noMovePossible(Colour colour) {
        return String.format("%s does not have other pieces in the board to move. "
                + "Ignoring the throw and moving on to the next player.", title(colour));
    }

    public String capture(Piece attacker, Piece victim, Position cell) {
        return String.format("%s piece %s lands on square %s, captures %s piece %s, and returns it to the base.",
                title(attacker.colour()), attacker.name(), cell.describe(attacker.colour()),
                title(victim.colour()), victim.name());
    }

    public String teleport(Piece piece, TeleportDestination destination) {
        return String.format("%s player lands on a mystery cell and is teleported to %s.%n%s piece %s teleported to %s.",
                title(piece.colour()), destination.displayName(),
                title(piece.colour()), piece.name(), destination.displayName());
    }

    public String effect(Piece piece, EffectNotice notice) {
        String colour = title(piece.colour());
        String name = piece.name();
        return switch (notice) {
            case ENERGISED -> String.format("%s piece %s feels energized, and movement speed doubles.", colour, name);
            case SICK -> String.format("%s piece %s feels sick, and movement speed halves.", colour, name);
            case BRIEFING -> String.format("%s piece %s attends briefing and cannot move for four rounds.",
                    colour, name);
            case BRIEFING_RETURN_TO_BASE -> String.format("%s piece %s is movement-restricted and has rolled three "
                    + "consecutively. Teleporting piece %s to base.", colour, name, name);
            case DIRECTION_REVERSED -> String.format("The %s piece %s, which was moving clockwise, has changed to "
                    + "moving counterclockwise.", colour, name);
            case SENT_TO_BETA -> String.format("The %s piece %s is moving in a counterclockwise direction. "
                    + "Teleporting to Beta from Gamma.", colour, name);
        };
    }

    public String mysteryCellSpawned(MysteryCell mysteryCell) {
        return String.format("A mystery cell has spawned in location %d and will be at this location for the next "
                + "four rounds.", mysteryCell.location());
    }

    public String roundSummary(List<PlayerStatusDto> statuses, MysteryCell mysteryCell) {
        StringBuilder summary = new StringBuilder();
        for (PlayerStatusDto status : statuses) {
            summary.append(playerStatus(status)).append(System.lineSeparator())
                    .append(pieceLocations(status));
        }
        return summary.append(mysteryCellLocation(mysteryCell)).toString();
    }

    public String playerFinished(Colour colour, int place) {
        if (place == WINNING_PLACE) {
            return String.format("%s player wins!!!", title(colour));
        }
        return String.format("%s player finishes in place %d.", title(colour), place);
    }

    public String gameStalled(int rounds) {
        return String.format("No piece can move any more after %d rounds (gridlock). The game ends here.", rounds);
    }

    private String pieceLocations(PlayerStatusDto status) {
        StringBuilder block = new StringBuilder()
                .append(SEPARATOR).append(System.lineSeparator())
                .append("Location of pieces ").append(title(status.colour())).append(System.lineSeparator())
                .append(SEPARATOR).append(System.lineSeparator());
        for (PieceLocationDto piece : status.pieces()) {
            block.append(String.format("Piece %s -> %s", piece.pieceName(), piece.location()))
                    .append(System.lineSeparator());
        }
        return block.toString();
    }

    private String mysteryCellLocation(MysteryCell mysteryCell) {
        if (!mysteryCell.isActive()) {
            return "There is no mystery cell on the board yet.";
        }
        return String.format("The mystery cell is at %d and will be at that location for the next %d rounds.",
                mysteryCell.location(), mysteryCell.roundsRemaining());
    }

    private String title(Colour colour) {
        String name = colour.displayName();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}