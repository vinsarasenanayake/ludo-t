package com.ludot.domain;

import java.util.ArrayList;
import java.util.List;

import static com.ludot.domain.BoardConstants.TRACK_SIZE;

public class Board {

    private final Cell[] cells = new Cell[TRACK_SIZE];

    public Board() {
        for (int index = 0; index < TRACK_SIZE; index++) {
            cells[index] = new Cell(index);
        }
    }

    public Cell cellAt(int index) {
        return cells[index];
    }

    public List<Piece> occupantsAt(int index) {
        return cellAt(index).occupants();
    }

    public boolean isBlockAt(int index) {
        return cellAt(index).isBlock();
    }

    public void enter(Piece piece, Direction direction) {
        piece.enterBoard(direction);
        cellAt(piece.position().index()).add(piece);
    }

    public void move(Piece piece, Position destination) {
        leaveCurrentCell(piece);
        piece.moveTo(destination);
        if (destination.isOnTrack()) {
            cellAt(destination.index()).add(piece);
        }
    }

    public void sendToBase(Piece piece) {
        leaveCurrentCell(piece);
        piece.returnToBase();
    }

    public List<Integer> emptyCellIndexes() {
        List<Integer> emptyCells = new ArrayList<>();
        for (Cell cell : cells) {
            if (cell.isEmpty()) {
                emptyCells.add(cell.index());
            }
        }
        return emptyCells;
    }

    private void leaveCurrentCell(Piece piece) {
        if (piece.isOnTrack()) {
            cellAt(piece.position().index()).remove(piece);
        }
    }
}