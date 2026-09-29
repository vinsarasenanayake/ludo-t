package com.ludot.infrastructure;

import com.ludot.domain.Colour;
import com.ludot.dto.GameResultDto;
import com.ludot.port.GameResultGateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvGameResultGateway implements GameResultGateway {

    private static final String FIELD_SEPARATOR = ",";
    private static final String ORDER_SEPARATOR = ";";

    private final Path file;

    public CsvGameResultGateway(Path file) {
        this.file = file;
    }

    @Override
    public void insert(GameResultDto result) {
        try {
            Files.writeString(file, toLine(result) + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new GameResultStorageException("Could not save game result to " + file, e);
        }
    }

    @Override
    public List<GameResultDto> findAll() {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file).stream()
                    .filter(line -> !line.isBlank())
                    .map(this::fromLine)
                    .toList();
        } catch (IOException e) {
            throw new GameResultStorageException("Could not read game results from " + file, e);
        }
    }

    private String toLine(GameResultDto result) {
        String order = result.finishingOrder().stream()
                .map(Colour::name)
                .collect(Collectors.joining(ORDER_SEPARATOR));
        return result.seed() + FIELD_SEPARATOR + result.rounds() + FIELD_SEPARATOR + order;
    }

    private GameResultDto fromLine(String line) {
        String[] fields = line.split(FIELD_SEPARATOR);
        List<Colour> order = Arrays.stream(fields[2].split(ORDER_SEPARATOR))
                .map(Colour::valueOf)
                .toList();
        return new GameResultDto(Long.parseLong(fields[0]), Integer.parseInt(fields[1]), order);
    }
}