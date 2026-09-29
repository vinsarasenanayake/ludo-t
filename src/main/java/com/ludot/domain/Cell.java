package com.ludot.domain;

import java.util.ArrayList;
import java.util.List;

public class Cell {

    private static final int MINIMUM_BLOCK_SIZE = 2;

    private final int index;
    private final List<Piece> occupants = new ArrayList<>();

    public Cell(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    public void add(Piece piece) {
        occupants.add(piece);
    }

    public void remove(Piece piece) {
        occupants.remove(piece);
    }

    public List<Piece> occupants() {
        return List.copyOf(occupants);
    }

    public boolean isEmpty() {
        return occupants.isEmpty();
    }

    public boolean isBlock() {
        return occupants.size() >= MINIMUM_BLOCK_SIZE && allOccupantsShareOneColour();
    }

    public boolean isBlockOwnedBy(Colour colour) {
        return isBlock() && occupants.get(0).colour() == colour;
    }

    public List<Piece> opponentsOf(Colour colour) {
        return occupants.stream()
                .filter(piece -> piece.colour() != colour)
                .toList();
    }

    private boolean allOccupantsShareOneColour() {
        Colour firstColour = occupants.get(0).colour();
        return occupants.stream().allMatch(piece -> piece.colour() == firstColour);
    }
}
