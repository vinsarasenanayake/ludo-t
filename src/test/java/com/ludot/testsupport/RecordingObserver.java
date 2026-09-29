package com.ludot.testsupport;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.TeleportDestination;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.port.EffectNotice;
import com.ludot.port.GameObserver;

import java.util.ArrayList;
import java.util.List;

public class RecordingObserver implements GameObserver {

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
    public void onRollIgnored(Colour colour) {
        events.add("roll ignored " + colour);
    }

    @Override
    public void onPieceEntered(Piece piece) {
        events.add("entered " + piece.name());
    }

    @Override
    public void onPieceMoved(Piece piece, Position from, int distance, Direction direction) {
        events.add("moved " + piece.name() + " to " + piece.position().describe(piece.colour()));
    }

    @Override
    public void onPieceBlocked(Piece piece, Position from, Position intendedDestination, Piece blockingPiece) {
        events.add("blocked " + piece.name() + " by " + blockingPiece.name());
    }

    @Override
    public void onMovedBeforeBlock(Colour colour, Position stoppedAt) {
        events.add("moved before block " + colour + " " + stoppedAt.describe(colour));
    }

    @Override
    public void onNoMovePossible(Colour colour) {
        events.add("no move " + colour);
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
    public void onTeleport(Piece piece, TeleportDestination destination) {
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
    public void onGameStalled(int rounds) {
        events.add("stalled " + rounds);
    }
}