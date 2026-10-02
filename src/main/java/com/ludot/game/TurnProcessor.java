package com.ludot.game;

import com.ludot.board.Board;
import com.ludot.board.Piece;
import com.ludot.movement.GameCommand;
import com.ludot.player.Player;
import com.ludot.random.Dice;

import static com.ludot.board.BoardConstants.ENTRY_ROLL;
import static com.ludot.board.BoardConstants.MAX_CONSECUTIVE_SIXES;

public final class TurnProcessor {

    private final Dice dice;
    private final Board board;
    private final RollResolver rollResolver;
    private final GameEvents.Turns listener;

    public TurnProcessor(Dice dice, Board board, RollResolver rollResolver, GameEvents.Turns listener) {
        this.dice = dice;
        this.board = board;
        this.rollResolver = rollResolver;
        this.listener = listener;
    }

    void playTurn(Player player) {
        int consecutiveSixes = 0;
        boolean rollAgain = true;
        while (rollAgain && !player.hasFinished()) {
            int roll = dice.roll();
            listener.onDiceRolled(player.colour(), roll);
            applyRollToBriefedPieces(player, roll);
            consecutiveSixes = roll == ENTRY_ROLL ? consecutiveSixes + 1 : 0;
            // R4 + T-6: the third six breaks blockades and ends the turn
            if (consecutiveSixes == MAX_CONSECUTIVE_SIXES) {
                breakBlockades(player);
                return;
            }
            GameCommand command = rollResolver.commandFor(player, roll);
            run(command, player);
            // R4 + T-2: a six or a capture rolls again, unless the throw was ignored
            rollAgain = !command.endsTurn() && (roll == ENTRY_ROLL || command.grantsBonusRoll());
        }
    }

    private void breakBlockades(Player player) {
        for (RollResolver.Breakaway breakaway : rollResolver.breakaways(player)) {
            rollResolver.breakAwayCommand(breakaway).ifPresent(command -> run(command, player));
        }
    }

    private void run(GameCommand command, Player player) {
        command.execute();
        if (command.showsPlayerStatus()) {
            listener.onPlayerStatus(player.status());
        }
    }

    // T-13: every roll counts towards a briefed piece's threes
    private void applyRollToBriefedPieces(Player player, int roll) {
        for (Piece piece : player.pieces()) {
            piece.observeRoll(roll);
            if (piece.requiresReturnToBase()) {
                listener.onSentBackFromBriefing(piece);
                board.sendToBase(piece);
            }
        }
    }
}
