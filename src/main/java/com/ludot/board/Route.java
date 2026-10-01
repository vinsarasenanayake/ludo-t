package com.ludot.board;

import java.util.Optional;

public record Route(Position from, Position destination, int distance,
                    int approachPassesGained, Optional<Blockage> blockage) {

    public record Blockage(Position intendedDestination, Piece blockingPiece) {
    }

    public static Route completed(Position from, Position destination, int distance, int approachPassesGained) {
        return new Route(from, destination, distance, approachPassesGained, Optional.empty());
    }

    public Route stoppedBy(Blockage newBlockage) {
        return new Route(from, destination, distance, approachPassesGained, Optional.of(newBlockage));
    }

    public boolean isCutShortByBlock() {
        return blockage.isPresent();
    }
}
