package com.ludot.player;

import com.ludot.board.TrackNavigator;
import com.ludot.rules.MoveOption;
import com.ludot.rules.MoveOption.Type;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

abstract class MoveRule implements PlayerStrategy {

    private final PlayerStrategy next;

    protected MoveRule(PlayerStrategy next) {
        this.next = next;
    }

    @Override
    public final MoveOption chooseMove(List<MoveOption> options) {
        return trySelect(options).orElseGet(() -> next.chooseMove(options));
    }

    protected abstract Optional<MoveOption> trySelect(List<MoveOption> options);

    private static Comparator<MoveOption> byLeadStepsToHome(TrackNavigator navigator) {
        return Comparator.comparingInt(option -> navigator.stepsToHome(option.leadPiece()));
    }

    static final class FirstAvailable implements PlayerStrategy {

        @Override
        public MoveOption chooseMove(List<MoveOption> options) {
            return options.get(0);
        }
    }

    static final class EnterFromBase extends MoveRule {

        EnterFromBase(PlayerStrategy next) {
            super(next);
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream().filter(option -> option.type() == Type.ENTER_BOARD).findFirst();
        }
    }

    static final class FormBlock extends MoveRule {

        FormBlock(PlayerStrategy next) {
            super(next);
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream()
                    .filter(MoveOption::formsBlock)
                    .filter(option -> !option.leavesBlock())
                    .findFirst();
        }
    }

    static final class BlockMove extends MoveRule {

        BlockMove(PlayerStrategy next) {
            super(next);
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream().filter(option -> option.type() == Type.MOVE_BLOCK).findFirst();
        }
    }

    static final class CaptureByPieceNeedingCapture extends MoveRule {

        CaptureByPieceNeedingCapture(PlayerStrategy next) {
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

    static final class CaptureClosestToVictimHome extends MoveRule {

        private final TrackNavigator navigator;

        CaptureClosestToVictimHome(TrackNavigator navigator, PlayerStrategy next) {
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

    static final class ClosestToHome extends MoveRule {

        private final TrackNavigator navigator;

        ClosestToHome(TrackNavigator navigator, PlayerStrategy next) {
            super(next);
            this.navigator = navigator;
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream().min(byLeadStepsToHome(navigator));
        }
    }

    static final class AvoidBlockClosestToHome extends MoveRule {

        private final TrackNavigator navigator;

        AvoidBlockClosestToHome(TrackNavigator navigator, PlayerStrategy next) {
            super(next);
            this.navigator = navigator;
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream()
                    .filter(option -> !option.formsBlock())
                    .min(byLeadStepsToHome(navigator));
        }
    }

    static final class NonBlockPieceClosestToHome extends MoveRule {

        private final TrackNavigator navigator;

        NonBlockPieceClosestToHome(TrackNavigator navigator, PlayerStrategy next) {
            super(next);
            this.navigator = navigator;
        }

        @Override
        protected Optional<MoveOption> trySelect(List<MoveOption> options) {
            return options.stream()
                    .filter(option -> option.type() == Type.MOVE_PIECE)
                    .filter(option -> !option.leavesBlock())
                    .min(byLeadStepsToHome(navigator));
        }
    }
}
