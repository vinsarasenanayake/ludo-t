package com.ludot.port;

import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.Route;
import com.ludot.dto.PlayerStatusDto;

import java.util.List;

public interface GameEvents {

    enum EffectNotice { ENERGISED, SICK, BRIEFING, DIRECTION_REVERSED, SENT_TO_BETA }

    interface TurnOrder {

        void onOpeningRoll(Colour colour, int value);

        void onTurnOrderDecided(List<Colour> order);
    }

    interface Rounds {

        void onPlayerIntroduced(Colour colour);

        void onRoundEnded(List<PlayerStatusDto> statuses, MysteryCell mysteryCell);

        void onPlayerFinished(Colour colour, int place);

        void onGameStalled(int rounds);
    }

    interface Turns {

        void onDiceRolled(Colour colour, int value);

        void onRollIgnored(Colour colour);

        void onPlayerStatus(PlayerStatusDto status);

        void onSentBackFromBriefing(Piece piece);
    }

    interface Moves {

        void onPieceEntered(Piece piece);

        void onPieceMoved(Piece piece, Route route, Direction direction);

        void onPieceBlocked(Piece piece, Position from, Route.Blockage blockage);

        void onMovedBeforeBlock(Colour colour, Position stoppedAt);

        void onNoMovePossible(Colour colour);

        void onCapture(Piece attacker, Piece victim, Position cell);
    }

    interface Mystery {

        void onMysteryCellSpawned(MysteryCell mysteryCell);

        void onTeleport(Piece piece, MysteryCell.Destination destination);

        void onEffectApplied(Piece piece, EffectNotice notice);
    }
}
