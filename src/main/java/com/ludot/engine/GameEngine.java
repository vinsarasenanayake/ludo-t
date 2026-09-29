package com.ludot.engine;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.player.Player;
import com.ludot.port.GameProgressListener;
import com.ludot.rules.MysteryCellManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GameEngine {

    // Assumption: in rare games every piece gets stuck (blocks and briefings). After 50 rounds
    // with no change at all, the game is declared stalled instead of looping forever.
    public static final int STALLED_ROUND_LIMIT = 50;

    private final Map<Colour, Player> players = new EnumMap<>(Colour.class);
    private final TurnProcessor turnProcessor;
    private final MysteryCellManager mysteryCells;
    private final StatusSnapshotFactory snapshots;
    private final GameProgressListener listener;

    public GameEngine(List<Player> players, TurnProcessor turnProcessor, MysteryCellManager mysteryCells,
                      StatusSnapshotFactory snapshots, GameProgressListener listener) {
        players.forEach(player -> this.players.put(player.colour(), player));
        this.turnProcessor = turnProcessor;
        this.mysteryCells = mysteryCells;
        this.snapshots = snapshots;
        this.listener = listener;
    }

    public void introducePlayers() {
        players.keySet().forEach(listener::onPlayerIntroduced);
    }

    public GameOutcome run(List<Colour> turnOrder) {
        List<Colour> finishingOrder = new ArrayList<>();
        List<PlayerStatusDto> previousStatuses = List.of();
        int rounds = 0;
        int unchangedRounds = 0;
        while (!isOver(finishingOrder) && unchangedRounds < STALLED_ROUND_LIMIT) {
            rounds++;
            playRound(turnOrder, finishingOrder);
            List<PlayerStatusDto> statuses = endRound(turnOrder);
            unchangedRounds = statuses.equals(previousStatuses) ? unchangedRounds + 1 : 0;
            previousStatuses = statuses;
        }
        boolean stalled = !isOver(finishingOrder);
        if (stalled) {
            listener.onGameStalled(rounds);
        }
        addUnfinishedPlayers(turnOrder, finishingOrder);
        return new GameOutcome(finishingOrder, rounds, stalled);
    }

    private void playRound(List<Colour> turnOrder, List<Colour> finishingOrder) {
        for (Colour colour : turnOrder) {
            Player player = players.get(colour);
            if (isOver(finishingOrder) || player.hasFinished()) {
                continue;
            }
            turnProcessor.playTurn(player);
            if (player.hasFinished()) {
                finishingOrder.add(colour);
                listener.onPlayerFinished(colour, finishingOrder.size());
            }
        }
    }

    private List<PlayerStatusDto> endRound(List<Colour> turnOrder) {
        players.values().forEach(player -> player.pieces().forEach(Piece::endRound));
        boolean anyPieceOnTrack = players.values().stream().anyMatch(Player::hasPieceOnTrack);
        mysteryCells.endRound(anyPieceOnTrack);
        List<PlayerStatusDto> statuses = turnOrder.stream()
                .map(colour -> snapshots.snapshotOf(players.get(colour)))
                .toList();
        listener.onRoundEnded(statuses, mysteryCells.current());
        return statuses;
    }

    // Rule 11: places are decided until only one player is left on the board.
    private boolean isOver(List<Colour> finishingOrder) {
        return finishingOrder.size() >= players.size() - 1;
    }

    private void addUnfinishedPlayers(List<Colour> turnOrder, List<Colour> finishingOrder) {
        for (Colour colour : turnOrder) {
            if (!finishingOrder.contains(colour)) {
                finishingOrder.add(colour);
            }
        }
    }
}
