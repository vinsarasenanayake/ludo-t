package com.ludot.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomSourcesTest {

    @Test
    @DisplayName("A six-sided die only rolls 1 to 6")
    void diceRollsStayBetweenOneAndSix() {
        SixSidedDice dice = new SixSidedDice(new Random(7));
        for (int roll = 0; roll < 1000; roll++) {
            int value = dice.roll();
            assertTrue(value >= 1 && value <= 6);
        }
    }

    @Test
    @DisplayName("The same seed gives the same game, which makes demos repeatable")
    void sameSeedGivesSameRolls() {
        SixSidedDice first = new SixSidedDice(new Random(42));
        SixSidedDice second = new SixSidedDice(new Random(42));
        for (int roll = 0; roll < 20; roll++) {
            assertEquals(first.roll(), second.roll());
        }
    }

    @Test
    void coinLandsOnBothSides() {
        FairCoin coin = new FairCoin(new Random(3));
        boolean sawHeads = false;
        boolean sawTails = false;
        for (int toss = 0; toss < 100; toss++) {
            boolean heads = coin.tossHeads();
            sawHeads |= heads;
            sawTails |= !heads;
        }
        assertTrue(sawHeads && sawTails);
    }

    @Test
    void cellPickerOnlyPicksAllowedCells() {
        RandomCellPicker picker = new RandomCellPicker(new Random(5));
        List<Integer> allowed = List.of(3, 17, 40);
        for (int pick = 0; pick < 50; pick++) {
            assertTrue(allowed.contains(picker.pick(allowed)));
        }
    }
}