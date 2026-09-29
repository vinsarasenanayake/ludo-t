package com.ludot.infrastructure;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.TeleportDestination;
import com.ludot.dto.PlayerStatusDto;
import com.ludot.port.EffectNotice;
import com.ludot.port.GameObserver;

import java.io.PrintStream;
import java.util.List;

public class ConsoleGameReporter implements GameObserver {

    private final MessageFormatter messages;
    private final PrintStream out;

    public ConsoleGameReporter(MessageFormatter messages, PrintStream out) {
        this.messages = messages;
        this.out = out;
    }

    @Override
    public void onPlayerIntroduced(Colour colour) {
        out.println(messages.playerIntroduction(colour));
    }

    @Override
    public void onOpeningRoll(Colour colour, int value) {
        out.println(messages.openingRoll(colour, value));
    }

    @Override
    public void onTurnOrderDecided(List<Colour> order) {
        out.println(messages.turnOrder(order));
    }

    @Override
    public void onDiceRolled(Colour colour, int value) {
        out.println(messages.diceRolled(colour, value));
    }

    @Override
    public void onRollIgnored(Colour colour) {
        out.println(messages.rollIgnored(colour));
    }

    @Override
    public void onPieceEntered(Piece piece) {
        out.println(messages.pieceEntered(piece));
    }

    @Override
    public void onPieceMoved(Piece piece, Position from, int distance, Direction direction) {
        out.println(messages.pieceMoved(piece, from, distance, direction));
    }

    @Override
    public void onPieceBlocked(Piece piece, Position from, Position intendedDestination, Piece blockingPiece) {
        out.println(messages.pieceBlocked(piece, from, intendedDestination, blockingPiece));
    }

    @Override
    public void onMovedBeforeBlock(Colour colour, Position stoppedAt) {
        out.println(messages.movedBeforeBlock(colour, stoppedAt));
    }

    @Override
    public void onNoMovePossible(Colour colour) {
        out.println(messages.noMovePossible(colour));
    }

    @Override
    public void onCapture(Piece attacker, Piece victim, Position cell) {
        out.println(messages.capture(attacker, victim, cell));
    }

    @Override
    public void onPlayerStatus(PlayerStatusDto status) {
        out.println(messages.playerStatus(status));
    }

    @Override
    public void onTeleport(Piece piece, TeleportDestination destination) {
        out.println(messages.teleport(piece, destination));
    }

    @Override
    public void onEffectApplied(Piece piece, EffectNotice notice) {
        out.println(messages.effect(piece, notice));
    }

    @Override
    public void onMysteryCellSpawned(MysteryCell mysteryCell) {
        out.println(messages.mysteryCellSpawned(mysteryCell));
    }

    @Override
    public void onRoundEnded(List<PlayerStatusDto> statuses, MysteryCell mysteryCell) {
        out.println(messages.roundSummary(statuses, mysteryCell));
        out.println();
    }

    @Override
    public void onPlayerFinished(Colour colour, int place) {
        out.println(messages.playerFinished(colour, place));
    }

    @Override
    public void onGameStalled(int rounds) {
        out.println(messages.gameStalled(rounds));
    }
}