package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.effect.EnergisedEffect;
import com.ludot.domain.effect.SickEffect;
import com.ludot.testsupport.FixedCoin;
import com.ludot.testsupport.RecordingObserver;
import com.ludot.testsupport.ScriptedDice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleporterTest {

    private static final int ALPHA_FACE = 1;
    private static final int BETA_FACE = 2;
    private static final int GAMMA_FACE = 3;
    private static final int BASE_FACE = 4;
    private static final int START_FACE = 5;
    private static final int APPROACH_FACE = 6;

    private Board board;
    private RecordingObserver observer;
    private Piece red1;

    @BeforeEach
    void setUp() {
        board = new Board();
        observer = new RecordingObserver();
        red1 = new Piece(Colour.RED, 1);
        board.enter(red1, Direction.CLOCKWISE);
        board.move(red1, Position.onTrack(30));
    }

    @Test
    @DisplayName("Rule T-12: Alpha with heads energises the piece")
    void alphaWithHeadsEnergises() {
        teleporterRolling(ALPHA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(7), red1.position());
        assertInstanceOf(EnergisedEffect.class, red1.effect());
    }

    @Test
    @DisplayName("Rule T-12: Alpha with tails makes the piece sick")
    void alphaWithTailsMakesSick() {
        teleporterRolling(ALPHA_FACE, false).teleport(red1);
        assertInstanceOf(SickEffect.class, red1.effect());
    }

    @Test
    @DisplayName("Rule T-13: Beta sends the piece to a briefing")
    void betaSendsToBriefing() {
        teleporterRolling(BETA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
    }

    @Test
    @DisplayName("Rule T-14: Gamma turns a clockwise piece counter-clockwise")
    void gammaReversesAClockwisePiece() {
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(44), red1.position());
        assertEquals(Direction.COUNTER_CLOCKWISE, red1.direction());
    }

    @Test
    @DisplayName("Rule T-14: Gamma sends a counter-clockwise piece on to Beta")
    void gammaSendsACounterClockwisePieceToBeta() {
        red1.reverseDirection();
        teleporterRolling(GAMMA_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(25), red1.position());
        assertFalse(red1.canMove());
    }

    @Test
    void baseSendsThePieceHome() {
        teleporterRolling(BASE_FACE, true).teleport(red1);
        assertTrue(red1.isInBase());
    }

    @Test
    void startSendsThePieceToItsX() {
        teleporterRolling(START_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(26), red1.position());
    }

    @Test
    @DisplayName("Assumption A19: teleporting to the approach counts as passing it")
    void approachSendsThePieceToItsApproachCell() {
        teleporterRolling(APPROACH_FACE, true).teleport(red1);
        assertEquals(Position.onTrack(24), red1.position());
        assertEquals(1, red1.approachPasses());
    }

    @Test
    void teleportIsReported() {
        teleporterRolling(BETA_FACE, true).teleport(red1);
        assertTrue(observer.hasEvent("teleport R1 BETA"));
    }

    private Teleporter teleporterRolling(int dieFace, boolean heads) {
        return new Teleporter(board, new ScriptedDice(dieFace), new FixedCoin(heads),
                new TrackNavigator(), new EffectFactory(), observer);
    }
}
