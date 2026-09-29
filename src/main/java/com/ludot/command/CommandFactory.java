package com.ludot.command;

import com.ludot.domain.Board;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.port.Coin;
import com.ludot.port.GameEvents;
import com.ludot.rules.MoveOption;
import com.ludot.rules.Teleporter;
import com.ludot.rules.TrackNavigator;

public class CommandFactory {

    private final Board board;
    private final Coin coin;
    private final TrackNavigator navigator;
    private final Teleporter teleporter;
    private final GameEvents.Moves listener;

    public CommandFactory(Board board, Coin coin, TrackNavigator navigator,
                          Teleporter teleporter, GameEvents.Moves listener) {
        this.board = board;
        this.coin = coin;
        this.navigator = navigator;
        this.teleporter = teleporter;
        this.listener = listener;
    }

    public GameCommand create(MoveOption option) {
        return switch (option.type()) {
            case ENTER_BOARD -> new EnterBoardCommand(option);
            case MOVE_PIECE -> new MovePieceCommand(option);
            case MOVE_BLOCK -> new MoveBlockCommand(option);
        };
    }

    public GameCommand createNoMove(Colour colour) {
        return new NullMoveCommand(colour);
    }

    private abstract class OptionCommand implements GameCommand {

        protected final MoveOption option;

        OptionCommand(MoveOption option) {
            this.option = option;
        }

        @Override
        public boolean grantsBonusRoll() {
            return option.capturesAny();
        }

        @Override
        public boolean showsPlayerStatus() {
            return option.capturesAny();
        }

        protected void land() {
            for (Piece piece : option.movers()) {
                for (int pass = 0; pass < option.route().approachPassesGained(); pass++) {
                    piece.recordApproachPass();
                }
            }
            if (option.capturesAny()) {
                captureVictims();
            }
            if (option.landsOnMysteryCell()) {
                option.movers().forEach(teleporter::teleport);
            }
        }

        private void captureVictims() {
            for (Piece victim : option.landing().victims()) {
                listener.onCapture(option.leadPiece(), victim, option.destination());
            }
            option.landing().victims().forEach(board::sendToBase);
            option.movers().forEach(Piece::recordCapture);
        }
    }

    private final class EnterBoardCommand extends OptionCommand {

        EnterBoardCommand(MoveOption option) {
            super(option);
        }

        @Override
        public void execute() {
            Piece piece = option.leadPiece();
            board.enter(piece, coin.tossHeads() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE);
            listener.onPieceEntered(piece);
            land();
        }

        @Override
        public boolean showsPlayerStatus() {
            return true;
        }
    }

    private final class MovePieceCommand extends OptionCommand {

        MovePieceCommand(MoveOption option) {
            super(option);
        }

        @Override
        public void execute() {
            Piece piece = option.leadPiece();
            Position from = piece.position();
            option.route().blockage().ifPresent(blockage -> listener.onPieceBlocked(piece, from, blockage));
            board.move(piece, option.destination());
            if (option.isCutShortByBlock()) {
                listener.onMovedBeforeBlock(piece.colour(), option.destination());
            } else {
                listener.onPieceMoved(piece, option.route(), piece.direction());
            }
            land();
        }
    }

    private final class MoveBlockCommand extends OptionCommand {

        MoveBlockCommand(MoveOption option) {
            super(option);
        }

        @Override
        public void execute() {
            Direction blockDirection = directionTravelled();
            for (Piece piece : option.movers()) {
                board.move(piece, option.destination());
                listener.onPieceMoved(piece, option.route(), blockDirection);
            }
            land();
        }

        private Direction directionTravelled() {
            int from = option.route().from().index();
            int clockwiseEnd = navigator.move(from, option.route().distance(), Direction.CLOCKWISE);
            return clockwiseEnd == option.destination().index() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
        }
    }

    private final class NullMoveCommand implements GameCommand {

        private final Colour colour;

        NullMoveCommand(Colour colour) {
            this.colour = colour;
        }

        @Override
        public void execute() {
            listener.onNoMovePossible(colour);
        }

        @Override
        public boolean grantsBonusRoll() {
            return false;
        }

        @Override
        public boolean showsPlayerStatus() {
            return false;
        }
    }
}
