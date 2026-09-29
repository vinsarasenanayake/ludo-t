package com.ludot.engine;

import com.ludot.command.CommandFactory;
import com.ludot.command.GameCommand;
import com.ludot.command.TurnOutcome;
import com.ludot.domain.Piece;
import com.ludot.player.Player;
import com.ludot.player.TurnContext;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.MoveType;
import com.ludot.rules.MysteryCellManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ludot.domain.BoardConstants.ENTRY_ROLL;

public class RollResolver {

    private final MovePlanner planner;
    private final CommandFactory commands;
    private final MysteryCellManager mysteryCells;

    public RollResolver(MovePlanner planner, CommandFactory commands, MysteryCellManager mysteryCells) {
        this.planner = planner;
        this.commands = commands;
        this.mysteryCells = mysteryCells;
    }

    public TurnOutcome resolve(Player player, int roll) {
        List<MoveOption> options = planner.findOptions(player.pieces(), roll, mysteryCells.current());
        GameCommand command = options.isEmpty()
                ? commands.createNoMove(player.colour())
                : commands.create(player.chooseMove(new TurnContext(roll, options, mysteryCells.current())));
        return command.execute();
    }

    public void breakBlockades(Player player) {
        for (Piece piece : piecesToBreakAway(player)) {
            planner.findOptions(List.of(piece), ENTRY_ROLL, mysteryCells.current()).stream()
                    .filter(option -> option.type() == MoveType.MOVE_PIECE)
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