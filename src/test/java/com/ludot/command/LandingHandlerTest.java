package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.rules.CaptureResolver;
import com.ludot.rules.EffectFactory;
import com.ludot.rules.Landing;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveType;
import com.ludot.rules.Route;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;
import com.ludot.testsupport.FixedCoin;
import com.ludot.testsupport.RecordingObserver;
import com.ludot.testsupport.ScriptedDice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LandingHandlerTest {

    private LandingHandler landingHandler;
    private Piece red1;

    @BeforeEach
    void setUp() {
        Board board = new Board();
        RecordingObserver observer = new RecordingObserver();
        Teleporter teleporter = new Teleporter(board, new ScriptedDice(), new FixedCoin(true),
                new TrackNavigator(), new EffectFactory(), observer);
        landingHandler = new LandingHandler(new CaptureResolver(board), teleporter, observer);
        red1 = new Piece(Colour.RED, 1);
    }

    @Test
    void recordsEveryApproachPassOfTheRoute() {
        landingHandler.resolve(optionGaining(2));
        assertEquals(2, red1.approachPasses());
    }

    @Test
    void landingWithoutVictimsCapturesNothing() {
        assertFalse(landingHandler.resolve(optionGaining(0)));
    }

    private MoveOption optionGaining(int approachPasses) {
        Route route = Route.completed(Position.onTrack(20), Position.onTrack(26), 6, approachPasses);
        return new MoveOption(MoveType.MOVE_PIECE, List.of(red1), route, Landing.offTrack(), false);
    }
}