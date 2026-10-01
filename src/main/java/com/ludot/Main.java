package com.ludot;

public final class  Main {

    static final class InvalidSeedException extends RuntimeException {

        InvalidSeedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final long DEFAULT_SEED = 42L;

    private Main() {
    }

    public static void main(String[] args) {
        try {
            long seed = args.length > 0 ? parseSeed(args[0]) : DEFAULT_SEED;
            new GameConfiguration(seed, System.out).createGame().play();
        } catch (InvalidSeedException e) {
            System.err.println(e.getMessage());
        }
    }

    static long parseSeed(String argument) {
        try {
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw new InvalidSeedException("The seed must be a whole number but was: " + argument, e);
        }
    }
}
