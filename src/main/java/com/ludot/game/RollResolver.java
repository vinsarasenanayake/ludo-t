package com.ludot.game;

import com.ludot.board.Piece;
import com.ludot.movement.CommandFactory;
import com.ludot.movement.GameCommand;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.player.Player;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MovePlanner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ludot.board.BoardConstants.BLOCKADE_BREAK_DISTANCE;

public final class RollResolver {

    private final MovePlanner planner;
    private final CommandFactory commands;
    private final MysteryCellManager mysteryCells;

    public RollResolver(MovePlanner planner, CommandFactory commands, MysteryCellManager mysteryCells) {
        this.planner = planner;
        this.commands = commands;
        this.mysteryCells = mysteryCells;
    }

    GameCommand commandFor(Player player, int roll) {
        List<MoveOption> options = planner.findOptions(player.pieces(), roll, mysteryCells.current());
        return options.isEmpty()
                ? commands.createNoMove(player.colour())
                : commands.create(player.chooseMove(options));
    }

    Optional<GameCommand> breakAwayCommand(Piece piece) {
        return planner.planSingleMove(piece, BLOCKADE_BREAK_DISTANCE, mysteryCells.current())
                .filter(option -> !option.isFullyBlocked())
                .map(commands::create);
    }

    List<Piece> piecesToBreakAway(Player player) {
        Map<Integer, List<Piece>> piecesByCell = new LinkedHashMap<>();
        for (Piece piece : player.pieces()) {
            if (piece.isOnTrack()) {
                piecesByCell.computeIfAbsent(piece.position().index(), cell -> new ArrayList<>()).add(piece);
            }
        }
        List<Piece> breakaways = new ArrayList<>();
        for (List<Piece> piecesOnCell : piecesByCell.values()) {
            breakaways.addAll(piecesOnCell.subList(1, piecesOnCell.size()));
        }
        return breakaways;
    }
}
