package com.ludot.rules;

import com.ludot.domain.Position;

import java.util.Optional;

public record Route(Position from, Position destination, int distance,
                    int approachPassesGained, Optional<Blockage> blockage) {

    public static Route completed(Position from, Position destination, int distance, int approachPassesGained) {
        return new Route(from, destination, distance, approachPassesGained, Optional.empty());
    }

    public static Route cutShort(Position from, Position destination, int distance,
                                 int approachPassesGained, Blockage blockage) {
        return new Route(from, destination, distance, approachPassesGained, Optional.of(blockage));
    }

    public boolean isCutShortByBlock() {
        return blockage.isPresent();
    }
}