package com.ludot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeleportDestinationTest {

    @Test
    @DisplayName("Rule T-11: die face 1 is Alpha")
    void faceOneIsAlpha() {
        assertEquals(TeleportDestination.ALPHA, TeleportDestination.fromDieFace(1));
    }

    @Test
    @DisplayName("Rule T-11: die face 6 is the approach cell")
    void faceSixIsApproach() {
        assertEquals(TeleportDestination.APPROACH, TeleportDestination.fromDieFace(6));
    }

    @Test
    void faceOutsideOneToSixIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> TeleportDestination.fromDieFace(7));
    }

    @Test
    void startIsDisplayedAsX() {
        assertEquals("X", TeleportDestination.START.displayName());
    }
}