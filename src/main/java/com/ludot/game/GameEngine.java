package com.ludot.game;

import com.ludot.board.Colour;
import com.ludot.board.Piece;
import com.ludot.mystery.MysteryCellManager;
import com.ludot.player.Player;
import com.ludot.player.PlayerStatusDto;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GameEngine {

    public static final int STALLED_ROUND_LIMIT = 50;

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

    public void introducePlayers() {
        players.keySet().forEach(listener::onPlayerIntroduced);
    }

    public GameResultDto run(List<Colour> turnOrder) {
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
        turnOrder.stream().filter(colour -> !finishingOrder.contains(colour)).forEach(finishingOrder::add);
        GameResultDto result = new GameResultDto(finishingOrder, rounds, stalled);
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
        players.values().forEach(player -> player.pieces().forEach(Piece::endRound));
        mysteryCells.endRound(players.values().stream().anyMatch(Player::hasPieceOnTrack));
        List<PlayerStatusDto> statuses = turnOrder.stream().map(colour -> players.get(colour).status()).toList();
        listener.onRoundEnded(statuses, mysteryCells.current());
        return statuses;
    }

    private boolean isOver(List<Colour> finishingOrder) {
        return finishingOrder.size() >= players.size() - 1;
    }
}
