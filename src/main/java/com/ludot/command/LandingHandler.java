package com.ludot.command;

import com.ludot.domain.Piece;
import com.ludot.rules.CaptureResolver;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;
import com.ludot.port.GameObserver;

import java.util.List;

public class LandingHandler {

    private final CaptureResolver captureResolver;
    private final Teleporter teleporter;
    private final GameObserver observer;

    public LandingHandler(CaptureResolver captureResolver, Teleporter teleporter, GameObserver observer) {
        this.captureResolver = captureResolver;
        this.teleporter = teleporter;
        this.observer = observer;
    }

    public boolean resolve(MoveOption option) {
        List<Piece> movers = option.movers();
        recordApproachPasses(movers, option.route().approachPassesGained());
        boolean captured = option.capturesAny();
        if (captured) {
            captureVictims(option);
        }
        if (option.landsOnMysteryCell()) {
            movers.forEach(teleporter::teleport);
        }
        return captured;
    }

    private void recordApproachPasses(List<Piece> movers, int passes) {
        for (Piece piece : movers) {
            for (int pass = 0; pass < passes; pass++) {
                piece.recordApproachPass();
            }
        }
    }

    private void captureVictims(MoveOption option) {
        for (Piece victim : option.landing().victims()) {
            observer.onCapture(option.leadPiece(), victim, option.destination());
        }
        captureResolver.capture(option.movers(), option.landing().victims());
    }
}