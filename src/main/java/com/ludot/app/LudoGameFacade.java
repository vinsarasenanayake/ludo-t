package com.ludot.app;

import com.ludot.domain.Colour;
import com.ludot.dto.GameResultDto;
import com.ludot.engine.GameEngine;
import com.ludot.engine.GameOutcome;
import com.ludot.engine.TurnOrderResolver;

import java.util.List;

// Facade: Main starts a whole game with one call and never touches the engine's parts.
public class LudoGameFacade {

    private final GameEngine engine;
    private final TurnOrderResolver turnOrderResolver;
    private final long seed;

    public LudoGameFacade(GameEngine engine, TurnOrderResolver turnOrderResolver, long seed) {
        this.engine = engine;
        this.turnOrderResolver = turnOrderResolver;
        this.seed = seed;
    }

    public GameResultDto play() {
        engine.introducePlayers();
        List<Colour> turnOrder = turnOrderResolver.resolve(List.of(Colour.values()));
        GameOutcome outcome = engine.run(turnOrder);
        return new GameResultDto(seed, outcome.rounds(), outcome.finishingOrder());
    }
}
