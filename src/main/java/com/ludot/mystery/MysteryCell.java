package com.ludot.mystery;

import static com.ludot.board.BoardConstants.TRACK_SIZE;

public interface MysteryCell {

    boolean isActive();

    boolean isAt(int cell);

    int location();

    int roundsRemaining();

    MysteryCell afterOneRound();

    enum None implements MysteryCell {
        INSTANCE;

        private static final int NO_LOCATION = -1;

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean isAt(int cell) {
            return false;
        }

        @Override
        public int location() {
            return NO_LOCATION;
        }

        @Override
        public int roundsRemaining() {
            return 0;
        }

        @Override
        public MysteryCell afterOneRound() {
            return this;
        }
    }

    record Active(int location, int roundsRemaining) implements MysteryCell {

        public Active {
            if (location < 0 || location >= TRACK_SIZE) {
                throw new IllegalArgumentException("Mystery cell must be on the track but was " + location);
            }
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isAt(int cell) {
            return location == cell;
        }

        @Override
        public MysteryCell afterOneRound() {
            return new Active(location, roundsRemaining - 1);
        }
    }

    enum Destination {
        ALPHA("Alpha"),
        BETA("Beta"),
        GAMMA("Gamma"),
        BASE("Base"),
        START("X"),
        APPROACH("Approach");

        private final String displayName;

        Destination(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public static Destination fromDieFace(int face) {
            Destination[] destinations = values();
            if (face < 1 || face > destinations.length) {
                throw new IllegalArgumentException("Die face must be 1 to " + destinations.length + " but was " + face);
            }
            return destinations[face - 1];
        }
    }
}
