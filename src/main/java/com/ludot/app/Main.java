package com.ludot.app;

public final class Main {

    public static class InvalidSeedException extends RuntimeException {

        public InvalidSeedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final long DEFAULT_SEED = 42L;

    private Main() {
    }

    public static void main(String[] args) {
        long seed = args.length > 0 ? parseSeed(args[0]) : DEFAULT_SEED;
        new LudoGameFacade(seed, System.out).play();
    }

    private static long parseSeed(String argument) {
        try {
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw new InvalidSeedException("The seed must be a whole number but was: " + argument, e);
        }
    }
}
