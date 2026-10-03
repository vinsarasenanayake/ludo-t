package com.ludot.board;

public final class Piece {

    // Guards against impossible moves
    public static final class IllegalMoveException extends RuntimeException {

        public IllegalMoveException(String message) {
            super(message);
        }
    }

    private final Colour colour;
    private final int number;
    private Position position;
    private Direction direction;
    private Direction originalDirection;
    private int captureCount;
    private int approachPasses;
    private PieceEffect effect;

    public Piece(Colour colour, int number) {
        this.colour = colour;
        this.number = number;
        returnToBase();
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

    // T-5, T-6: the direction set by the coin toss on X
    public Direction originalDirection() {
        return originalDirection;
    }

    public int approachPasses() {
        return approachPasses;
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

    void enterBoard(Direction chosenDirection) {
        if (!isInBase()) {
            throw new IllegalMoveException(name() + " can only enter the board from base");
        }
        position = Position.onTrack(colour.startCell());
        direction = chosenDirection;
        originalDirection = chosenDirection;
    }

    void moveTo(Position destination) {
        if (isHome()) {
            throw new IllegalMoveException(name() + " is already home and cannot move");
        }
        position = destination;
    }

    public void reverseDirection() {
        direction = direction.opposite();
    }

    public void restoreOriginalDirection() {
        direction = originalDirection;
    }

    public void recordCapture() {
        captureCount++;
    }

    public void recordApproachPass() {
        approachPasses++;
    }

    // The current effect decides how far the piece moves (T-12, T-13)
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
            effect = PieceEffect.None.INSTANCE;
        }
    }

    void returnToBase() {
        position = Position.base();
        direction = Direction.CLOCKWISE;
        originalDirection = Direction.CLOCKWISE;
        captureCount = 0;
        approachPasses = 0;
        effect = PieceEffect.None.INSTANCE;
    }

    @Override
    public String toString() {
        return name();
    }
}