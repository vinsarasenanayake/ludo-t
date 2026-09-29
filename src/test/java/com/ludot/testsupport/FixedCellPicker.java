package com.ludot.testsupport;

import com.ludot.port.CellPicker;

import java.util.List;

public class FixedCellPicker implements CellPicker {

    private final int preferredCell;

    public FixedCellPicker(int preferredCell) {
        this.preferredCell = preferredCell;
    }

    @Override
    public int pick(List<Integer> candidateCells) {
        return candidateCells.contains(preferredCell) ? preferredCell : candidateCells.get(0);
    }
}
