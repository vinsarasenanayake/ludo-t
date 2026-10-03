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

public final class RollResolver {

    // T-6: unequal shares of six, so the leaving pieces split up
    private static final List<List<Integer>> BREAKAWAY_SHARES = List.of(List.of(6), List.of(4, 2), List.of(3, 2, 1));

    record Breakaway(Piece piece, int steps) {
    }

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
        // No legal move gives the Null Object command
        return options.isEmpty()
                ? commands.createNoMove()
                : commands.create(player.chooseMove(options));
    }

    List<Breakaway> breakaways(Player player) {
        List<Breakaway> breakaways = new ArrayList<>();
        for (List<Piece> blockade : blockadesOf(player)) {
            List<Piece> leavers = blockade.subList(1, blockade.size());
            List<Integer> shares = BREAKAWAY_SHARES.get(leavers.size() - 1);
            for (int index = 0; index < leavers.size(); index++) {
                breakaways.add(new Breakaway(leavers.get(index), shares.get(index)));
            }
        }
        return breakaways;
    }

    // A breakaway cut short by a block stays where it is
    Optional<GameCommand> breakAwayCommand(Breakaway breakaway) {
        return planner.planPieceMove(breakaway.piece(), breakaway.steps(), mysteryCells.current())
                .filter(option -> !option.isCutShortByBlock())
                .map(commands::create);
    }

    private static List<List<Piece>> blockadesOf(Player player) {
        Map<Integer, List<Piece>> piecesByCell = new LinkedHashMap<>();
        for (Piece piece : player.pieces()) {
            if (piece.isOnTrack()) {
                piecesByCell.computeIfAbsent(piece.position().index(), cell -> new ArrayList<>()).add(piece);
            }
        }
        return piecesByCell.values().stream().filter(pieces -> pieces.size() > 1).toList();
    }
}