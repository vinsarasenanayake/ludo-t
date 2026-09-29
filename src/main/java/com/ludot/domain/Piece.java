package com.ludot.domain;

import com.ludot.domain.effect.NoEffect;
import com.ludot.domain.effect.PieceEffect;

public class Piece {

    private final Colour colour;
    private final int number;
    private Position position;
    private Direction direction;
    private int captureCount;
    private int approachPasses;
    private PieceEffect effect;

    public Piece(Colour colour, int number) {
        this.colour = colour;
        this.number = number;
        resetToBase();
    }

    public String name() {
        return String.valueOf(colour.initial()) + number;
    }

    public Colour colour() {
        return colour;
    }

    public int number() {
        return number;
    }

    public Position position() {
        return position;
    }

    public Direction direction() {
        return direction;
    }

    public int captureCount() {
        return captureCount;
    }

    public int approachPasses() {
        return approachPasses;
    }

    public PieceEffect effect() {
        return effect;
    }

    public boolean isInBase() {
        return position.isInBase();
    }

    public boolean isOnTrack() {
        return position.isOnTrack();
    }

    public boolean isInHomeStraight() {
        return position.isInHomeStraight();
    }

    public boolean isHome() {
        return position.isHome();
    }

    public boolean hasCaptured() {
        return captureCount > 0;
    }

    public void enterBoard(Direction chosenDirection) {
        if (!isInBase()) {
            throw new IllegalMoveException(name() + " can only enter the board from base");
        }
        position = Position.onTrack(colour.startCell());
        direction = chosenDirection;
    }

    public void moveTo(Position destination) {
        if (isHome()) {
            throw new IllegalMoveException(name() + " is already home and cannot move");
        }
        position = destination;
    }

    public void reverseDirection() {
        direction = direction.opposite();
    }

    public void recordCapture() {
        captureCount++;
    }

    public void recordApproachPass() {
        approachPasses++;
    }

    public void applyEffect(PieceEffect newEffect) {
        effect = newEffect;
    }

    public int adjustRoll(int roll) {
        return effect.adjustRoll(roll);
    }

    public boolean canMove() {
        return !isHome() && effect.canMove();
    }

    public void observeRoll(int roll) {
        effect.observeRoll(roll);
    }

    public boolean requiresReturnToBase() {
        return effect.requiresReturnToBase();
    }

    public void endRound() {
        effect.endRound();
        if (effect.isExpired()) {
            effect = NoEffect.INSTANCE;
        }
    }

    // Rule T-9: a captured piece loses everything it had (captures, passes, direction, effect).
    public void returnToBase() {
        resetToBase();
    }

    @Override
    public String toString() {
        return name();
    }

    private void resetToBase() {
        position = Position.base();
        direction = Direction.CLOCKWISE;
        captureCount = 0;
        approachPasses = 0;
        effect = NoEffect.INSTANCE;
    }
}
