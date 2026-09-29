package com.ludot.port;

import com.ludot.dto.GameResultDto;

import java.util.List;

public interface GameResultGateway {

    void insert(GameResultDto result);

    List<GameResultDto> findAll();
}