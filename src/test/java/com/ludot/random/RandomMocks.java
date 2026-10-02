package com.ludot.random;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Mocked dice, coin, and cell picker with fixed results
public final class RandomMocks {

    private RandomMocks() {
    }

    // Returns the given rolls in order
    public static Dice diceRolling(Integer... rolls) {
        Dice dice = mock(Dice.class);
        when(dice.roll()).thenReturn(rolls[0], Arrays.copyOfRange(rolls, 1, rolls.length));
        return dice;
    }

    public static Coin coinLanding(boolean heads) {
        Coin coin = mock(Coin.class);
        when(coin.tossHeads()).thenReturn(heads);
        return coin;
    }

    public static CellPicker pickerChoosing(int cell) {
        CellPicker cellPicker = mock(CellPicker.class);
        when(cellPicker.pick(anyList())).thenReturn(cell);
        return cellPicker;
    }
}