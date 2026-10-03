package com.ludot.movement;

import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;

public interface MoveEvents {

    void onPieceEntered(Piece piece);

    void onPieceMoved(Piece piece, Route route, Direction direction);

    void onPieceBlocked(Piece piece, Position from, Route.Blockage blockage);

    void onMovedBeforeBlock(Colour colour, Position stoppedAt);

    void onBlockedThrowIgnored(Colour colour);

    void onCapture(Piece attacker, Piece victim, Position cell);
}