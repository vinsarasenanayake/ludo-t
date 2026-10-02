package com.ludot.game;

import com.ludot.board.Colour;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.player.Player;
import com.ludot.player.PlayerStatusDto;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class GameEngine {

    // Ends a stuck game after 50 rounds with no change
    private static final int STALLED_ROUND_LIMIT = 50;

    private final Map<Colour, Player> players = new EnumMap<>(Colour.class);
    private final TurnProcessor turnProcessor;
    private final MysteryCellManager mysteryCells;
    private final GameEvents.Rounds listener;

    public GameEngine(List<Player> players, TurnProcessor turnProcessor, MysteryCellManager mysteryCells,
                      GameEvents.Rounds listener) {
        players.forEach(player -> this.players.put(player.colour(), player));
        this.turnProcessor = turnProcessor;
        this.mysteryCells = mysteryCells;
        this.listener = listener;
    }

    void introducePlayers() {
        players.keySet().forEach(listener::onPlayerIntroduced);
    }

    GameResultDto run(List<Colour> turnOrder) {
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
        // R11: the last player left takes the final place
        if (isOver(finishingOrder)) {
            turnOrder.stream().filter(colour -> !finishingOrder.contains(colour)).forEach(finishingOrder::add);
        }
        GameResultDto result = new GameResultDto(finishingOrder, rounds);
        listener.onGameOver(result);
        return result;
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
        players.values().forEach(Player::endRound);
        // T-10: counting starts once a piece is on the track
        mysteryCells.endRound(players.values().stream().anyMatch(Player::hasPieceOnTrack));
        List<PlayerStatusDto> statuses = turnOrder.stream().map(colour -> players.get(colour).status()).toList();
        listener.onRoundEnded(statuses, mysteryCells.current());
        return statuses;
    }

    // R11: the game ends when three players have finished
    private boolean isOver(List<Colour> finishingOrder) {
        return finishingOrder.size() >= players.size() - 1;
    }
}