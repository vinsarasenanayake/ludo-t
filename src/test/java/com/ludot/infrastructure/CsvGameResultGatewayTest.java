package com.ludot.infrastructure;

import com.ludot.domain.Colour;
import com.ludot.dto.GameResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvGameResultGatewayTest {

    private Path folder;

    @BeforeEach
    void setUp() throws IOException {
        folder = Files.createTempDirectory("ludo-t-results");
    }

    @Test
    void noFileMeansNoResults() {
        CsvGameResultGateway gateway = new CsvGameResultGateway(folder.resolve("missing.csv"));
        assertTrue(gateway.findAll().isEmpty());
    }

    @Test
    @DisplayName("Data Gateway: a saved result can be read back")
    void insertedResultCanBeFoundAgain() {
        CsvGameResultGateway gateway = new CsvGameResultGateway(folder.resolve("results.csv"));
        GameResultDto result = new GameResultDto(42L, 180, List.of(Colour.BLUE, Colour.RED, Colour.GREEN, Colour.YELLOW));

        gateway.insert(result);

        assertEquals(List.of(result), gateway.findAll());
    }

    @Test
    @DisplayName("Storage errors are wrapped in an unchecked exception that keeps the cause")
    void storageFailureIsWrapped() {
        CsvGameResultGateway gateway = new CsvGameResultGateway(folder);
        GameResultDto result = new GameResultDto(1L, 10, List.of(Colour.RED));
        GameResultStorageException error = assertThrows(GameResultStorageException.class, () -> gateway.insert(result));
        assertTrue(error.getCause() instanceof IOException);
    }
}