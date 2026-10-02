package com.ludot.board;

// All fixed numbers from the brief
public final class BoardConstants {

    // Board and pieces
    public static final int TRACK_SIZE = 52;
    public static final int HOME_STRAIGHT_LENGTH = 5;
    public static final int PIECES_PER_PLAYER = 4;
    public static final int APPROACH_OFFSET_FROM_START = 2;

    // Dice rules (R2, R4)
    public static final int ENTRY_ROLL = 6;
    public static final int MAX_CONSECUTIVE_SIXES = 3;

    // Mystery cell (T-10 to T-13)
    public static final int EFFECT_DURATION_ROUNDS = 4;
    public static final int MYSTERY_SPAWN_DELAY_ROUNDS = 2;
    public static final int MYSTERY_LIFETIME_ROUNDS = 4;

    // T-11: counted from Yellow's approach cell
    public static final int ALPHA_OFFSET = 9;
    public static final int BETA_OFFSET = 27;
    public static final int GAMMA_OFFSET = 46;

    // Constants only, never created
    private BoardConstants() {
    }
}