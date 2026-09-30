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

    public void playTurn(Player player) {
        int consecutiveSixes = 0;
        boolean rollAgain = true;
        while (rollAgain && !player.hasFinished()) {
            int roll = dice.roll();
            listener.onDiceRolled(player.colour(), roll);
            checkBriefings(player, roll);
            consecutiveSixes = roll == ENTRY_ROLL ? consecutiveSixes + 1 : 0;
            if (consecutiveSixes == MAX_CONSECUTIVE_SIXES) {
                listener.onRollIgnored(player.colour());
                rollResolver.breakBlockades(player);
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
