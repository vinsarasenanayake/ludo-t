package com.ludot.engine;

import com.ludot.domain.Colour;
import com.ludot.port.Dice;
import com.ludot.port.GameObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TurnOrderResolver {

    private final Dice dice;
    private final GameObserver observer;

    public TurnOrderResolver(Dice dice, GameObserver observer) {
        this.dice = dice;
        this.observer = observer;
    }

    public List<Colour> resolve(List<Colour> colours) {
        List<Colour> contenders = colours;
        while (contenders.size() > 1) {
            contenders = highestRollers(contenders);
        }
        List<Colour> order = clockwiseOrderFrom(contenders.get(0), colours.size());
        observer.onTurnOrderDecided(order);
        return order;
    }

    private List<Colour> highestRollers(List<Colour> contenders) {
        Map<Colour, Integer> rolls = new EnumMap<>(Colour.class);
        for (Colour colour : contenders) {
            int roll = dice.roll();
            observer.onOpeningRoll(colour, roll);
            rolls.put(colour, roll);
        }
        int highest = Collections.max(rolls.values());
        return contenders.stream()
                .filter(colour -> rolls.get(colour) == highest)
                .toList();
    }

    private List<Colour> clockwiseOrderFrom(Colour first, int playerCount) {
        List<Colour> order = new ArrayList<>();
        Colour current = first;
        for (int turn = 0; turn < playerCount; turn++) {
            order.add(current);
            current = current.nextClockwise();
        }
        return order;
    }
}