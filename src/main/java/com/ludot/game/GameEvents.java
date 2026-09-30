package com.ludot.game;

import com.ludot.board.Colour;
import com.ludot.board.Piece;
import com.ludot.mystery.MysteryCell;
import com.ludot.player.PlayerStatusDto;

import java.util.List;

public interface GameEvents {

    interface TurnOrder {

        void onOpeningRoll(Colour colour, int value);

        void onTurnOrderDecided(List<Colour> order);
    }

    interface Rounds {

        void onPlayerIntroduced(Colour colour);

        void onRoundEnded(List<PlayerStatusDto> statuses, MysteryCell mysteryCell);

        void onPlayerFinished(Colour colour, int place);

        void onGameStalled(int rounds);

        void onGameOver(GameResultDto result);
    }

    interface Turns {

        void onDiceRolled(Colour colour, int value);

        void onRollIgnored(Colour colour);

        void onPlayerStatus(PlayerStatusDto status);

        void onSentBackFromBriefing(Piece piece);
    }
}
