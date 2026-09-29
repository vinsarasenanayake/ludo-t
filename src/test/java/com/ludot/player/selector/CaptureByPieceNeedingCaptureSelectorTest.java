package com.ludot.player.selector;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveOption;
import org.junit.jupiter.api.Test;

import static com.ludot.player.selector.SelectorTestSupport.NEXT;
import static com.ludot.player.selector.SelectorTestSupport.PASSED_ON;
import static com.ludot.testsupport.MoveOptions.capture;
import static com.ludot.testsupport.MoveOptions.context;
import static org.junit.jupiter.api.Assertions.assertSame;

class CaptureByPieceNeedingCaptureSelectorTest {

    private final CaptureByPieceNeedingCaptureSelector selector = new CaptureByPieceNeedingCaptureSelector(NEXT);
    private final Piece yellow1 = new Piece(Colour.YELLOW, 1);
    private final Piece red1 = new Piece(Colour.RED, 1);

    @Test
    void picksACaptureByAPieceWithNoCapturesYet() {
        MoveOption capture = capture(yellow1, red1);
        assertSame(capture, selector.select(context(capture)));
    }

    @Test
    void passesOnWhenThePieceAlreadyCaptured() {
        yellow1.recordCapture();
        assertSame(PASSED_ON, selector.select(context(capture(yellow1, red1))));
    }
}
