package com.ludot.engine;

import com.ludot.command.TurnOutcome;
import com.ludot.player.Player;
import com.ludot.port.Dice;
import com.ludot.port.GameObserver;

import static com.ludot.domain.BoardConstants.ENTRY_ROLL;
import static com.ludot.domain.BoardConstants.MAX_CONSECUTIVE_SIXES;

public class TurnProcessor {

    private final Dice dice;
    private final RollResolver rollResolver;
    private final BriefingMonitor briefingMonitor;
    private final StatusSnapshotFactory snapshots;
    private final GameObserver observer;

    public TurnProcessor(Dice dice, RollResolver rollResolver, BriefingMonitor briefingMonitor,
                         StatusSnapshotFactory snapshots, GameObserver observer) {
        this.dice = dice;
        this.rollResolver = rollResolver;
        this.briefingMonitor = briefingMonitor;
        this.snapshots = snapshots;
        this.observer = observer;
    }

    public void playTurn(Player player) {
        int consecutiveSixes = 0;
        boolean rollAgain = true;
        while (rollAgain && !player.hasFinished()) {
            int roll = dice.roll();
            observer.onDiceRolled(player.colour(), roll);
            briefingMonitor.observeRoll(player, roll);
            consecutiveSixes = roll == ENTRY_ROLL ? consecutiveSixes + 1 : 0;
            if (consecutiveSixes == MAX_CONSECUTIVE_SIXES) {
                ignoreThirdSix(player);
                return;
            }
            TurnOutcome outcome = rollResolver.resolve(player, roll);
            reportStatusIfChanged(player, outcome);
            rollAgain = roll == ENTRY_ROLL || outcome.grantsBonusRoll();
        }
    }

    private void ignoreThirdSix(Player player) {
        rollResolver.breakBlockades(player);
        observer.onRollIgnored(player.colour());
    }

    private void reportStatusIfChanged(Player player, TurnOutcome outcome) {
        if (outcome.boardCountsChanged()) {
            observer.onPlayerStatus(snapshots.snapshotOf(player));
        }
    }
}