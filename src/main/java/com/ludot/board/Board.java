package com.ludot.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.ludot.board.BoardConstants.TRACK_SIZE;

// The 52 track cells; a list per cell because T-3 allows several pieces
public final class Board {

    private static final int MINIMUM_BLOCK_SIZE = 2;

    private final List<List<Piece>> cells = new ArrayList<>();

    public Board() {
        for (int index = 0; index < TRACK_SIZE; index++) {
            cells.add(new ArrayList<>());
        }
    }

    public List<Piece> occupantsAt(int index) {
        return List.copyOf(cells.get(index));
    }

    public List<Piece> opponentsAt(int index, Colour colour) {
        return cells.get(index).stream()
                .filter(piece -> piece.colour() != colour)
                .toList();
    }

    // T-3: two own pieces make a block
    public boolean isBlockOwnedBy(int index, Colour colour) {
        long ownPieces = cells.get(index).stream().filter(piece -> piece.colour() == colour).count();
        return ownPieces >= MINIMUM_BLOCK_SIZE;
    }

    public boolean isOpponentBlockAt(int index, Colour colour) {
        return Arrays.stream(Colour.values())
                .anyMatch(other -> other != colour && isBlockOwnedBy(index, other));
    }

    public List<Integer> emptyCellIndexes() {
        return IntStream.range(0, TRACK_SIZE)
                .filter(index -> cells.get(index).isEmpty())
                .boxed()
                .toList();
    }

    public void enter(Piece piece, Direction direction) {
        piece.enterBoard(direction);
        cells.get(piece.position().index()).add(piece);
    }

    public void move(Piece piece, Position destination) {
        leaveCurrentCell(piece);
        piece.moveTo(destination);
        if (destination.isOnTrack()) {
            cells.get(destination.index()).add(piece);
        }
    }

    public void sendToBase(Piece piece) {
        leaveCurrentCell(piece);
        piece.returnToBase();
    }

    private void leaveCurrentCell(Piece piece) {
        if (piece.isOnTrack()) {
            cells.get(piece.position().index()).remove(piece);
        }
    }
}