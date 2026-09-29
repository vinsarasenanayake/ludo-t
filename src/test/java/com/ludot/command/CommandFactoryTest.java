package com.ludot.command;

import com.ludot.domain.Colour;
import com.ludot.domain.Piece;
import com.ludot.rules.MoveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CommandFactoryTest {

    private CommandTestFixture fixture;
    private CommandFactory factory;
    private Piece red1;
    private Piece red2;

    @BeforeEach
    void setUp() {
        fixture = new CommandTestFixture();
        factory = fixture.factoryWith(true);
        red1 = new Piece(Colour.RED, 1);
        red2 = new Piece(Colour.RED, 2);
    }

    @Test
    void entryOptionBecomesEnterBoardCommand() {
        GameCommand command = factory.create(fixture.option(List.of(red1), 6, MoveType.ENTER_BOARD));
        assertInstanceOf(EnterBoardCommand.class, command);
    }

    @Test
    void pieceOptionBecomesMovePieceCommand() {
        fixture.placeOnTrack(red1, 26);
        GameCommand command = factory.create(fixture.option(List.of(red1), 3, MoveType.MOVE_PIECE));
        assertInstanceOf(MovePieceCommand.class, command);
    }

    @Test
    void blockOptionBecomesMoveBlockCommand() {
        fixture.placeOnTrack(red1, 0);
        fixture.placeOnTrack(red2, 0);
        GameCommand command = factory.create(fixture.option(List.of(red1, red2), 4, MoveType.MOVE_BLOCK));
        assertInstanceOf(MoveBlockCommand.class, command);
    }

    @Test
    @DisplayName("Null Object: no legal move gives a NullMoveCommand, never null")
    void noMoveBecomesNullMoveCommand() {
        assertInstanceOf(NullMoveCommand.class, factory.createNoMove(Colour.RED));
    }
}