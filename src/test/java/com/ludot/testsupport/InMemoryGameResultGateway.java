package com.ludot.testsupport;

import com.ludot.dto.GameResultDto;
import com.ludot.port.GameResultGateway;

import java.util.ArrayList;
import java.util.List;

public class InMemoryGameResultGateway implements GameResultGateway {

    private final List<GameResultDto> results = new ArrayList<>();

    @Override
    public void insert(GameResultDto result) {
        results.add(result);
    }

    @Override
    public List<GameResultDto> findAll() {
        return List.copyOf(results);
    }
}