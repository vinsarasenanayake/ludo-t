package com.ludot.random;

import java.util.List;

// Picks the mystery cell from the free cells (T-10)
public interface CellPicker {

    int pick(List<Integer> candidateCells);
}