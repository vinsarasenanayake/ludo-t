package com.ludot.rules;

import com.ludot.domain.Piece;
import com.ludot.domain.Position;

public record Blockage(Position intendedDestination, Piece blockingPiece) {
}