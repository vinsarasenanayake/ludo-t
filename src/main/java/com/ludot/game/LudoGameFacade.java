package com.ludot.game;

import com.ludot.board.Colour;

import java.util.List;

// Facade: one call plays the whole game
public final class LudoGameFacade {

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
