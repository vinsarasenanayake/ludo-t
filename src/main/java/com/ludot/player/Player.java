package com.ludot.player;

import com.ludot.board.Colour;
import com.ludot.board.Piece;
import com.ludot.rules.MoveOption;

import java.util.ArrayList;
import java.util.List;

import static com.ludot.board.BoardConstants.PIECES_PER_PLAYER;

public final class Player {

    private final Colour colour;
    private final PlayerStrategy strategy;
    private final List<Piece> pieces = new ArrayList<>();

    Player(Colour colour, PlayerStrategy strategy) {
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

    int piecesInBase() {
        return (int) pieces.stream().filter(Piece::isInBase).count();
    }

    int piecesOnBoard() {
        return (int) pieces.stream().filter(piece -> piece.isOnTrack() || piece.isInHomeStraight()).count();
    }

    public boolean hasPieceOnTrack() {
        return pieces.stream().anyMatch(Piece::isOnTrack);
    }

    public boolean hasFinished() {
        return pieces.stream().allMatch(Piece::isHome);
    }

    public MoveOption chooseMove(List<MoveOption> options) {
        return strategy.chooseMove(options);
    }

    public PlayerStatusDto status() {
        List<PlayerStatusDto.PieceLocation> locations = pieces.stream()
                .map(piece -> new PlayerStatusDto.PieceLocation(piece.name(), piece.position().describe(colour)))
                .toList();
        return new PlayerStatusDto(colour, piecesOnBoard(), piecesInBase(), locations);
    }
}
