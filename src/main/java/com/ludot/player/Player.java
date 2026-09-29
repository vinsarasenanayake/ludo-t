package com.ludot.player;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;

import java.util.ArrayList;
import java.util.List;

import static com.ludot.domain.BoardConstants.PIECES_PER_PLAYER;

public class Player {

    private final Colour colour;
    private final PlayerStrategy strategy;
    private final List<Piece> pieces = new ArrayList<>();

    public Player(Colour colour, PlayerStrategy strategy) {
        this.colour = colour;
        this.strategy = strategy;
        for (int number = 1; number <= PIECES_PER_PLAYER; number++) {
            pieces.add(new Piece(colour, number));
        }
    }

    public Colour colour() {
        return colour;
    }

    public List<Piece> pieces() {
        return List.copyOf(pieces);
    }

    public int piecesInBase() {
        return (int) pieces.stream().filter(Piece::isInBase).count();
    }

    public int piecesOnBoard() {
        return (int) pieces.stream().filter(piece -> piece.isOnTrack() || piece.isInHomeStraight()).count();
    }

    public boolean hasPieceOnTrack() {
        return pieces.stream().anyMatch(Piece::isOnTrack);
    }

    public boolean hasFinished() {
        return pieces.stream().allMatch(Piece::isHome);
    }

    public MoveOption chooseMove(TurnContext context) {
        return strategy.chooseMove(context);
    }
}