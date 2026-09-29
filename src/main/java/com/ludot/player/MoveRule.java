package com.ludot.player;

import com.ludot.board.TrackNavigator;
import com.ludot.rules.MoveOption.Type;
import com.ludot.rules.MoveOption;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class MoveRule implements PlayerStrategy {

    private final PlayerStrategy next;

    protected MoveRule(PlayerStrategy next) {
        this.next = next;
    }

    @Override
    public final MoveOption chooseMove(List<MoveOption> options) {
        return trySelect(options).orElseGet(() -> next.chooseMove(options));
    }

    protected abstract Optional<MoveOption> trySelect(List<MoveOption> options);

    public static final class FirstAvailable implements PlayerStrategy {

        @Override
        public MoveOption chooseMove(List<MoveOption> options) {
            return options.get(0);
        }
    }

    public static final class EnterFromBase extends MoveRule {

        public EnterFromBase(PlayerStrategy next) {
            super(next);
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream().filter(option -> option.type() == Type.ENTER_BOARD).findFirst();
        }
    }

    public static final class FormBlock extends MoveRule {

        public FormBlock(PlayerStrategy next) {
            super(next);
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream().filter(MoveOption::formsBlock).findFirst();
        }
    }

    public static final class BlockMove extends MoveRule {

        public BlockMove(PlayerStrategy next) {
            super(next);
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream().filter(option -> option.type() == Type.MOVE_BLOCK).findFirst();
        }
    }

    public static final class CaptureByPieceNeedingCapture extends MoveRule {

        public CaptureByPieceNeedingCapture(PlayerStrategy next) {
            super(next);
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream()
                    .filter(MoveOption::capturesAny)
                    .filter(option -> !option.leadPiece().hasCaptured())
                    .findFirst();
        }
    }

    public static final class CaptureClosestToVictimHome extends MoveRule {

        private final TrackNavigator navigator;

        public CaptureClosestToVictimHome(TrackNavigator navigator, PlayerStrategy next) {
            super(next);
            this.navigator = navigator;
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream()
                    .filter(MoveOption::capturesAny)
                    .min(Comparator.comparingInt(this::closestVictimStepsToHome));
        }

        private int closestVictimStepsToHome(MoveOption option) {
            return option.landing().victims().stream()
                    .mapToInt(navigator::stepsToHome)
                    .min()
                    .orElse(TrackNavigator.NOT_ON_BOARD);
        }
    }

    public static final class ClosestToHome extends MoveRule {

        private final TrackNavigator navigator;

        public ClosestToHome(TrackNavigator navigator, PlayerStrategy next) {
            super(next);
            this.navigator = navigator;
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream().min(Comparator.comparingInt(option -> navigator.stepsToHome(option.leadPiece())));
        }
    }

    public static final class AvoidBlockClosestToHome extends MoveRule {

        private final TrackNavigator navigator;

        public AvoidBlockClosestToHome(TrackNavigator navigator, PlayerStrategy next) {
            super(next);
            this.navigator = navigator;
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream()
                    .filter(option -> !option.formsBlock())
                    .min(Comparator.comparingInt(option -> navigator.stepsToHome(option.leadPiece())));
        }
    }

    public static final class NonBlockPieceClosestToHome extends MoveRule {

        private final TrackNavigator navigator;

        public NonBlockPieceClosestToHome(TrackNavigator navigator, PlayerStrategy next) {
            super(next);
            this.navigator = navigator;
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream()
                    .filter(option -> option.type() == Type.MOVE_PIECE)
                    .filter(option -> !option.leavesBlock())
                    .min(Comparator.comparingInt(option -> navigator.stepsToHome(option.leadPiece())));
        }
    }
}
