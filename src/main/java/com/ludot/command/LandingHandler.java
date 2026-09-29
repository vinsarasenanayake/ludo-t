package com.ludot.command;

import com.ludot.domain.Piece;
import com.ludot.port.MoveListener;
import com.ludot.rules.CaptureResolver;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;

import java.util.List;

// What happens after pieces arrive: approach passes are recorded, then captures, then the mystery cell.
public class LandingHandler {

    private final CaptureResolver captureResolver;
    private final Teleporter teleporter;
    private final MoveListener listener;

    public LandingHandler(CaptureResolver captureResolver, Teleporter teleporter, MoveListener listener) {
        this.captureResolver = captureResolver;
        this.teleporter = teleporter;
        this.listener = listener;
    }

    public void resolve(MoveOption option) {
        List<Piece> movers = option.movers();
        recordApproachPasses(movers, option.route().approachPassesGained());
        if (option.capturesAny()) {
            captureVictims(option);
        }
        if (option.landsOnMysteryCell()) {
            movers.forEach(teleporter::teleport);
        }
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
            listener.onCapture(option.leadPiece(), victim, option.destination());
        }
        captureResolver.capture(option.movers(), option.landing().victims());
    }
}
