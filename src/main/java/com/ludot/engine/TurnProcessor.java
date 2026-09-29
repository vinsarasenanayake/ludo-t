package com.ludot.engine;

import com.ludot.command.TurnOutcome;
import com.ludot.player.Player;
import com.ludot.port.Dice;
import com.ludot.port.TurnListener;

import static com.ludot.domain.BoardConstants.ENTRY_ROLL;
import static com.ludot.domain.BoardConstants.MAX_CONSECUTIVE_SIXES;

// One player's turn: a six or a capture (Rule T-2) earns another roll; a third six in a row is ignored.
public class TurnProcessor {

    private final Dice dice;
    private final RollResolver rollResolver;
    private final BriefingMonitor briefingMonitor;
    private final StatusSnapshotFactory snapshots;
    private final TurnListener listener;

    public TurnProcessor(Dice dice, RollResolver rollResolver, BriefingMonitor briefingMonitor,
                         StatusSnapshotFactory snapshots, TurnListener listener) {
        this.dice = dice;
        this.rollResolver = rollResolver;
        this.briefingMonitor = briefingMonitor;
        this.snapshots = snapshots;
        this.listener = listener;
    }

    public void playTurn(Player player) {
        int consecutiveSixes = 0;
        boolean rollAgain = true;
        while (rollAgain && !player.hasFinished()) {
            int roll = dice.roll();
            listener.onDiceRolled(player.colour(), roll);
            briefingMonitor.observeRoll(player, roll);
            consecutiveSixes = roll == ENTRY_ROLL ? consecutiveSixes + 1 : 0;
            if (consecutiveSixes == MAX_CONSECUTIVE_SIXES) {
                ignoreThirdSix(player);
                return;
            }
            TurnOutcome outcome = rollResolver.resolve(player, roll);
            reportStatusIfNeeded(player, outcome);
            rollAgain = roll == ENTRY_ROLL || outcome.grantsBonusRoll();
        }
    }

    // Rule T-6: before the dice passes on, any blockade is broken up.
    private void ignoreThirdSix(Player player) {
        rollResolver.breakBlockades(player);
        listener.onRollIgnored(player.colour());
    }

    // Section 3.1 shows the status line only after a piece enters the board or captures.
    private void reportStatusIfNeeded(Player player, TurnOutcome outcome) {
        if (outcome.showsPlayerStatus()) {
            listener.onPlayerStatus(snapshots.snapshotOf(player));
        }
    }
}
