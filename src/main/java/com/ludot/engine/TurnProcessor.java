package com.ludot.engine;

import com.ludot.command.CommandFactory;
import com.ludot.command.GameCommand;
import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.player.Player;
import com.ludot.port.Dice;
import com.ludot.port.GameEvents;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MovePlanner;
import com.ludot.rules.MysteryCellManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ludot.domain.BoardConstants.ENTRY_ROLL;
import static com.ludot.domain.BoardConstants.MAX_CONSECUTIVE_SIXES;

public class TurnProcessor {

    private final Dice dice;
    private final Board board;
    private final MovePlanner planner;
    private final CommandFactory commands;
    private final MysteryCellManager mysteryCells;
    private final GameEvents.Turns listener;

    public TurnProcessor(Dice dice, Board board, MovePlanner planner, CommandFactory commands,
                         MysteryCellManager mysteryCells, GameEvents.Turns listener) {
        this.dice = dice;
        this.board = board;
        this.planner = planner;
        this.commands = commands;
        this.mysteryCells = mysteryCells;
        this.listener = listener;
    }

    public void playTurn(Player player) {
        int consecutiveSixes = 0;
        boolean rollAgain = true;
        while (rollAgain && !player.hasFinished()) {
            int roll = dice.roll();
            listener.onDiceRolled(player.colour(), roll);
            checkBriefings(player, roll);
            consecutiveSixes = roll == ENTRY_ROLL ? consecutiveSixes + 1 : 0;
            if (consecutiveSixes == MAX_CONSECUTIVE_SIXES) {
                breakBlockades(player);
                listener.onRollIgnored(player.colour());
                return;
            }
            GameCommand command = commandFor(player, roll);
            command.execute();
            if (command.showsPlayerStatus()) {
                listener.onPlayerStatus(player.status());
            }
            rollAgain = roll == ENTRY_ROLL || command.grantsBonusRoll();
        }
    }

    private GameCommand commandFor(Player player, int roll) {
        List<MoveOption> options = planner.findOptions(player.pieces(), roll, mysteryCells.current());
        return options.isEmpty()
                ? commands.createNoMove(player.colour())
                : commands.create(player.chooseMove(options));
    }

    private void checkBriefings(Player player, int roll) {
        for (Piece piece : player.pieces()) {
            piece.observeRoll(roll);
            if (piece.requiresReturnToBase()) {
                listener.onSentBackFromBriefing(piece);
                board.sendToBase(piece);
            }
        }
    }

    void breakBlockades(Player player) {
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
