package com.ludot.engine;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.effect.BriefingEffect;
import com.ludot.player.Player;
import com.ludot.testsupport.RecordingObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BriefingMonitorTest {

    private Board board;
    private BriefingMonitor monitor;
    private Piece red1;
    private Player red;

    @BeforeEach
    void setUp() {
        board = new Board();
        monitor = new BriefingMonitor(board, new RecordingObserver());
        red = new Player(Colour.RED, context -> context.options().get(0));
        red1 = red.pieces().get(0);
        board.enter(red1, Direction.CLOCKWISE);
        red1.applyEffect(new BriefingEffect());
    }

    @Test
    @DisplayName("Rule T-13: two threes in a row send a briefed piece to base")
    void twoThreesSendTheBriefedPieceToBase() {
        monitor.observeRoll(red, 3);
        monitor.observeRoll(red, 3);
        assertTrue(red1.isInBase());
    }

    @Test
    void otherRollsLeaveThePieceWhereItIs() {
        monitor.observeRoll(red, 3);
        monitor.observeRoll(red, 4);
        assertFalse(red1.isInBase());
    }
}
