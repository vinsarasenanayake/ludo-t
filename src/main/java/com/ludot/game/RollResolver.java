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

import static com.ludot.board.BoardConstants.ENTRY_ROLL;

public class RollResolver {

    private final MovePlanner planner;
    private final CommandFactory commands;
    private final MysteryCellManager mysteryCells;

    public RollResolver(MovePlanner planner, CommandFactory commands, MysteryCellManager mysteryCells) {
        this.planner = planner;
        this.commands = commands;
        this.mysteryCells = mysteryCells;
    }

    public GameCommand commandFor(Player player, int roll) {
        List<MoveOption> options = planner.findOptions(player.pieces(), roll, mysteryCells.current());
        return options.isEmpty()
                ? commands.createNoMove(player.colour())
                : commands.create(player.chooseMove(options));
    }

    public void breakBlockades(Player player) {
        for (Piece piece : piecesToBreakAway(player)) {
            planner.findOptions(List.of(piece), ENTRY_ROLL, mysteryCells.current()).stream()
                    .filter(option -> option.type() == MoveOption.Type.MOVE_PIECE)
                    .findFirst()
                    .ifPresent(option -> commands.create(option).execute());
        }
    }

    private List<Piece> piecesToBreakAway(Player player) {
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
