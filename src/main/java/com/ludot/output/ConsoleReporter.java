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

import java.io.PrintStream;
import java.util.List;

import static com.ludot.board.BoardConstants.PIECES_PER_PLAYER;

public final class ConsoleReporter implements GameObserver {

    private static final String SEPARATOR = "============================";
    private static final int WINNING_PLACE = 1;

    private final PrintStream out;

    public ConsoleReporter(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onPlayerIntroduced(Colour colour) {
        char initial = colour.initial();
        print("The %s player has four (04) pieces named %s1, %s2, %s3, and %s4.",
                colour.displayName(), initial, initial, initial, initial);
    }

    @Override
    public void onOpeningRoll(Colour colour, int value) {
        print("%s rolls %d", colour.title(), value);
    }

    @Override
    public void onTurnOrderDecided(List<Colour> order) {
        List<String> names = order.stream().map(Colour::title).toList();
        print("%s player has the highest roll and will begin the game.", names.get(0));
        print("The order of a single round is %s, and %s.",
                String.join(", ", names.subList(0, names.size() - 1)), names.get(names.size() - 1));
    }

    @Override
    public void onDiceRolled(Colour colour, int value) {
        print("%s player rolled %d.", colour.title(), value);
    }

    @Override
    public void onRollIgnored(Colour colour) {
        print("%s rolled six three times in a row. The roll is ignored and the dice passes to the next player.",
                colour.title());
    }

    @Override
    public void onPlayerStatus(PlayerStatusDto status) {
        print("%s player now has %d/%d on pieces on the board and %d/%d pieces on the base.",
                status.colour().title(), status.piecesOnBoard(), PIECES_PER_PLAYER,
                status.piecesInBase(), PIECES_PER_PLAYER);
    }

    @Override
    public void onSentBackFromBriefing(Piece piece) {
        print("%s piece %s is movement-restricted and has rolled three consecutively. Teleporting piece %s to base.",
                piece.colour().title(), piece.name(), piece.name());
    }

    @Override
    public void onPieceEntered(Piece piece) {
        print("%s player moves piece %s to the starting point.", piece.colour().title(), piece.name());
    }

    @Override
    public void onPieceMoved(Piece piece, Route route, Direction direction) {
        print("%s moves piece %s from location %s to %s by %d units in %s direction.",
                piece.colour().title(), piece.name(), route.from().describe(piece.colour()),
                piece.position().describe(piece.colour()), route.distance(), direction.displayName());
    }

    @Override
    public void onPieceBlocked(Piece piece, Position from, Route.Blockage blockage) {
        Piece blocker = blockage.blockingPiece();
        print("%s piece %s is blocked from moving from %s to %s by %s piece %s.",
                piece.colour().title(), piece.name(), from.describe(piece.colour()),
                blockage.intendedDestination().describe(piece.colour()), blocker.colour().title(), blocker.name());
    }

    @Override
    public void onMovedBeforeBlock(Colour colour, Position stoppedAt) {
        print("%s does not have other pieces in the board to move instead of the blocked piece. "
                + "Moved the piece to square %s which is the cell before the block.",
                colour.title(), stoppedAt.describe(colour));
    }

    @Override
    public void onNoMovePossible(Colour colour) {
        print("%s does not have other pieces in the board to move. "
                + "Ignoring the throw and moving on to the next player.",
                colour.title());
    }

    @Override
    public void onBlockedThrowIgnored(Colour colour) {
        print("%s does not have other pieces in the board to move instead of the blocked piece. "
                + "Ignoring the throw and moving on to the next player.", colour.title());
    }

    @Override
    public void onCapture(Piece attacker, Piece victim, Position cell) {
        print("%s piece %s lands on square %s, captures %s piece %s, and returns it to the base.",
                attacker.colour().title(), attacker.name(), cell.describe(attacker.colour()),
                victim.colour().title(), victim.name());
    }

    @Override
    public void onTeleport(Piece piece, MysteryCell.Destination destination) {
        String colour = piece.colour().title();
        print("%s player lands on a mystery cell and is teleported to %s.", colour, destination.displayName());
        print("%s piece %s teleported to %s.", colour, piece.name(), destination.displayName());
    }

    @Override
    public void onEffectApplied(Piece piece, EffectNotice notice) {
        String colour = piece.colour().title();
        String name = piece.name();
        switch (notice) {
            case ENERGISED -> print("%s piece %s feels energized, and movement speed doubles.", colour, name);
            case SICK -> print("%s piece %s feels sick, and movement speed halves.", colour, name);
            case BRIEFING -> print("%s piece %s attends briefing and cannot move for four rounds.", colour, name);
            case DIRECTION_REVERSED -> print("The %s piece %s, which was moving clockwise, has changed to "
                    + "moving counterclockwise.", colour, name);
            case SENT_TO_BETA -> print("The %s piece %s is moving in a counterclockwise direction. "
                    + "Teleporting to Beta from Gamma.", colour, name);
        }
    }

    @Override
    public void onMysteryCellSpawned(MysteryCell mysteryCell) {
        print("A mystery cell has spawned in location %d and will be at this location for the next four rounds.",
                mysteryCell.location());
    }

    @Override
    public void onRoundEnded(List<PlayerStatusDto> statuses, MysteryCell mysteryCell) {
        for (PlayerStatusDto status : statuses) {
            onPlayerStatus(status);
            print(SEPARATOR);
            print("Location of pieces %s", status.colour().title());
            print(SEPARATOR);
            status.pieces().forEach(piece -> print("Piece %s -> %s", piece.pieceName(), piece.location()));
        }
        if (mysteryCell.isActive()) {
            int rounds = mysteryCell.roundsRemaining();
            print("The mystery cell is at %d and will be at that location for the next %d %s.",
                    mysteryCell.location(), rounds, rounds == 1 ? "round" : "rounds");
        } else {
            print("There is no mystery cell on the board yet.");
        }
        out.println();
    }

    @Override
    public void onPlayerFinished(Colour colour, int place) {
        if (place == WINNING_PLACE) {
            print("%s player wins!!!", colour.title());
        } else {
            print("%s player finishes in place %d.", colour.title(), place);
        }
    }

    @Override
    public void onGameStalled(int rounds) {
        print("No piece can move any more after %d rounds (gridlock). The game ends here.", rounds);
    }

    @Override
    public void onGameOver(GameResultDto result) {
        print(SEPARATOR);
        print("Final results after %d rounds", result.rounds());
        print(SEPARATOR);
        List<Colour> ranking = result.finishingOrder();
        for (int place = 1; place <= ranking.size(); place++) {
            print("%s place: %s", ordinal(place), ranking.get(place - 1).title());
        }
        if (result.stalled()) {
            print("Players who did not reach home are listed in turn order.");
        }
    }

    private static String ordinal(int place) {
        return switch (place) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> place + "th";
        };
    }

    private void print(String format, Object... values) {
        out.println(String.format(format, values));
    }
}
