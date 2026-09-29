package com.ludot.app;

import com.ludot.domain.Colour;
import com.ludot.dto.GameResultDto;
import com.ludot.engine.GameEngine;
import com.ludot.engine.TurnOrderResolver;

import java.util.List;

public class LudoGameFacade {

    private final GameEngine engine;
    private final TurnOrderResolver turnOrderResolver;

    public LudoGameFacade(GameEngine engine, TurnOrderResolver turnOrderResolver) {
        this.engine = engine;
        this.turnOrderResolver = turnOrderResolver;
    }

    public GameResultDto play() {
        engine.introducePlayers();
        return engine.run(turnOrderResolver.resolve(List.of(Colour.values())));
    }
}
