package com.ludot.port;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.TeleportDestination;
import com.ludot.dto.PlayerStatusDto;

import java.util.List;

public interface GameObserver {

    void onPlayerIntroduced(Colour colour);

    void onOpeningRoll(Colour colour, int value);

    void onTurnOrderDecided(List<Colour> order);

    void onDiceRolled(Colour colour, int value);

    void onRollIgnored(Colour colour);

    void onPieceEntered(Piece piece);

    void onPieceMoved(Piece piece, Position from, int distance, Direction direction);

    void onPieceBlocked(Piece piece, Position from, Position intendedDestination, Piece blockingPiece);

    void onMovedBeforeBlock(Colour colour, Position stoppedAt);

    void onNoMovePossible(Colour colour);

    void onCapture(Piece attacker, Piece victim, Position cell);

    void onPlayerStatus(PlayerStatusDto status);

    void onTeleport(Piece piece, TeleportDestination destination);

    void onEffectApplied(Piece piece, EffectNotice notice);

    void onMysteryCellSpawned(MysteryCell mysteryCell);

    void onRoundEnded(List<PlayerStatusDto> statuses, MysteryCell mysteryCell);

    void onPlayerFinished(Colour colour, int place);

    void onGameStalled(int rounds);
}