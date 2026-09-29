package com.ludot.engine;

import com.ludot.command.GameCommand;
import com.ludot.domain.Board;
import com.ludot.domain.Piece;
import com.ludot.player.Player;
import com.ludot.port.Dice;
import com.ludot.port.GameEvents;

import static com.ludot.domain.BoardConstants.ENTRY_ROLL;
import static com.ludot.domain.BoardConstants.MAX_CONSECUTIVE_SIXES;

public class TurnProcessor {

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

    public void playTurn(Player player) {
        int consecutiveSixes = 0;
        boolean rollAgain = true;
        while (rollAgain && !player.hasFinished()) {
            int roll = dice.roll();
            listener.onDiceRolled(player.colour(), roll);
            checkBriefings(player, roll);
            consecutiveSixes = roll == ENTRY_ROLL ? consecutiveSixes + 1 : 0;
            if (consecutiveSixes == MAX_CONSECUTIVE_SIXES) {
                rollResolver.breakBlockades(player);
                listener.onRollIgnored(player.colour());
                return;
            }
            GameCommand command = rollResolver.commandFor(player, roll);
            command.execute();
            if (command.showsPlayerStatus()) {
                listener.onPlayerStatus(player.status());
            }
            rollAgain = roll == ENTRY_ROLL || command.grantsBonusRoll();
        }
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
}
