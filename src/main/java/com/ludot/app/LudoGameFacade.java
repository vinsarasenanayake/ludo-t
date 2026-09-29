package com.ludot.app;

import com.ludot.domain.Colour;
import com.ludot.dto.GameResultDto;
import com.ludot.engine.GameEngine;
import com.ludot.engine.GameOutcome;
import com.ludot.engine.TurnOrderResolver;
import com.ludot.port.GameResultGateway;

import java.util.List;

public class LudoGameFacade {

    private final GameEngine engine;
    private final TurnOrderResolver turnOrderResolver;
    private final GameResultGateway results;
    private final long seed;

    public LudoGameFacade(GameEngine engine, TurnOrderResolver turnOrderResolver,
                          GameResultGateway results, long seed) {
        this.engine = engine;
        this.turnOrderResolver = turnOrderResolver;
        this.results = results;
        this.seed = seed;
    }

    public GameResultDto play() {
        engine.introducePlayers();
        List<Colour> turnOrder = turnOrderResolver.resolve(List.of(Colour.values()));
        GameOutcome outcome = engine.run(turnOrder);
        GameResultDto result = new GameResultDto(seed, outcome.rounds(), outcome.finishingOrder());
        results.insert(result);
        return result;
    }
}