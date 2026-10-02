package com.ludot.game;

import com.ludot.board.Colour;
import com.ludot.random.Dice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TurnOrderResolver {

    private final Dice dice;
    private final GameEvents.TurnOrder listener;

    public TurnOrderResolver(Dice dice, GameEvents.TurnOrder listener) {
        this.dice = dice;
        this.listener = listener;
    }

    List<Colour> resolve(List<Colour> colours) {
        List<Colour> contenders = colours;
        // Only the tied highest players roll again
        while (contenders.size() > 1) {
            contenders = highestRollers(contenders);
        }
        List<Colour> order = new ArrayList<>();
        Colour current = contenders.getFirst();
        // Then play goes clockwise from the winner
        for (int turn = 0; turn < colours.size(); turn++) {
            order.add(current);
            current = current.nextClockwise();
        }
        listener.onTurnOrderDecided(order);
        return order;
    }

    private List<Colour> highestRollers(List<Colour> contenders) {
        Map<Colour, Integer> rolls = new EnumMap<>(Colour.class);
        for (Colour colour : contenders) {
            int roll = dice.roll();
            listener.onOpeningRoll(colour, roll);
            rolls.put(colour, roll);
        }
        int highest = Collections.max(rolls.values());
        return contenders.stream().filter(colour -> rolls.get(colour) == highest).toList();
    }
}