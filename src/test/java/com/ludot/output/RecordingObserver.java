package com.ludot.output;

import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;
import com.ludot.game.GameResultDto;
import com.ludot.mystery.MysteryCell;
import com.ludot.mystery.MysteryEvents.EffectNotice;
import com.ludot.player.PlayerStatusDto;

import java.util.ArrayList;
import java.util.List;

// Test spy: records each event as a short line of text
public final class RecordingObserver implements GameListener {

    private final List<String> events = new ArrayList<>();

    public List<String> events() {
        return List.copyOf(events);
    }

    public boolean hasEvent(String event) {
        return events.contains(event);
    }

    @Override
    public void onPlayerIntroduced(Colour colour) {
        events.add("introduced " + colour);
    }

    @Override
    public void onOpeningRoll(Colour colour, int value) {
        events.add("opening roll " + colour + " " + value);
    }

    @Override
    public void onTurnOrderDecided(List<Colour> order) {
        events.add("order " + order);
    }

    @Override
    public void onDiceRolled(Colour colour, int value) {
        events.add("rolled " + colour + " " + value);
    }

    @Override
    public void onSentBackFromBriefing(Piece piece) {
        events.add("briefing return " + piece.name());
    }

    @Override
    public void onPieceEntered(Piece piece) {
        events.add("entered " + piece.name());
    }

    @Override
    public void onPieceMoved(Piece piece, Route route, Direction direction) {
        events.add("moved " + piece.name() + " to " + piece.position().describe(piece.colour()));
    }

    @Override
    public void onPieceBlocked(Piece piece, Position from, Route.Blockage blockage) {
        events.add("blocked " + piece.name() + " by " + blockage.blockingPiece().name());
    }

    @Override
    public void onMovedBeforeBlock(Colour colour, Position stoppedAt) {
        events.add("moved before block " + colour + " " + stoppedAt.describe(colour));
    }

    @Override
    public void onBlockedThrowIgnored(Colour colour) {
        events.add("blocked throw ignored " + colour);
    }

    @Override
    public void onCapture(Piece attacker, Piece victim, Position cell) {
        events.add("capture " + attacker.name() + " " + victim.name());
    }

    @Override
    public void onPlayerStatus(PlayerStatusDto status) {
        events.add("status " + status.colour() + " " + status.piecesOnBoard() + "/" + status.piecesInBase());
    }

    @Override
    public void onTeleport(Piece piece, MysteryCell.Destination destination, Position location) {
        events.add("teleport " + piece.name() + " " + destination);
    }

    @Override
    public void onEffectApplied(Piece piece, EffectNotice notice) {
        events.add("effect " + piece.name() + " " + notice);
    }

    @Override
    public void onMysteryCellSpawned(MysteryCell mysteryCell) {
        events.add("mystery spawned " + mysteryCell.location());
    }

    @Override
    public void onRoundEnded(List<PlayerStatusDto> statuses, MysteryCell mysteryCell) {
        events.add("round ended");
    }

    @Override
    public void onPlayerFinished(Colour colour, int place) {
        events.add("finished " + colour + " " + place);
    }

    @Override
    public void onGameStalled(int unchangedRounds) {
        events.add("stalled after " + unchangedRounds);
    }

    @Override
    public void onGameOver(GameResultDto result) {
        events.add("game over " + result.finishingOrder());
    }
}