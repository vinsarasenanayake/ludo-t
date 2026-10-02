package com.ludot.board;

import java.util.Optional;

// A traced move: where it ends and whether a block stopped it
public record Route(Position from, Position destination, int distance,
                    int approachPassesGained, Optional<Blockage> blockage) {

    // T-3: where the piece wanted to go and who blocked it
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
