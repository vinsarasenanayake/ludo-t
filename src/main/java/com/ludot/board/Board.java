package com.ludot.board;

import java.util.ArrayList;
import java.util.List;

import static com.ludot.board.BoardConstants.TRACK_SIZE;

public class Board {

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

    public boolean isBlockAt(int index) {
        List<Piece> occupants = cells.get(index);
        return occupants.size() >= MINIMUM_BLOCK_SIZE
                && occupants.stream().allMatch(piece -> piece.colour() == occupants.get(0).colour());
    }

    public boolean isBlockOwnedBy(int index, Colour colour) {
        return isBlockAt(index) && cells.get(index).get(0).colour() == colour;
    }

    public List<Integer> emptyCellIndexes() {
        List<Integer> emptyCells = new ArrayList<>();
        for (int index = 0; index < TRACK_SIZE; index++) {
            if (cells.get(index).isEmpty()) {
                emptyCells.add(index);
            }
        }
        return emptyCells;
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
