package com.ludot.rules;

import com.ludot.board.Board;
import com.ludot.board.Colour;
import com.ludot.board.Direction;
import com.ludot.board.Piece;
import com.ludot.board.Position;
import com.ludot.board.Route;
import com.ludot.board.TrackNavigator;
import com.ludot.mystery.MysteryCell;
import com.ludot.rules.MoveOption.Landing;
import com.ludot.rules.MoveOption.Type;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ludot.board.BoardConstants.ENTRY_ROLL;
import static com.ludot.board.BoardConstants.HOME_STRAIGHT_LENGTH;

public final class MovePlanner {

    private static final int SINGLE_PIECE = 1;

    private final Board board;
    private final TrackNavigator navigator;

    public MovePlanner(Board board, TrackNavigator navigator) {
        this.board = board;
        this.navigator = navigator;
    }

    public List<MoveOption> findOptions(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        List<MoveOption> options = new ArrayList<>();
        for (Piece piece : pieces) {
            planEntry(piece, roll, mysteryCell).ifPresent(options::add);
        }
        for (Piece piece : pieces) {
            planPieceMove(piece, roll, mysteryCell).ifPresent(options::add);
        }
        options.addAll(planBlockMoves(pieces, roll, mysteryCell));
        return withoutBlockedMovesIfOthersExist(options);
    }

    private List<MoveOption> withoutBlockedMovesIfOthersExist(List<MoveOption> options) {
        List<MoveOption> fullMoves = options.stream().filter(option -> !option.isCutShortByBlock()).toList();
        if (!fullMoves.isEmpty()) {
            return fullMoves;
        }
        List<MoveOption> partialMoves = options.stream().filter(option -> !option.isFullyBlocked()).toList();
        return partialMoves.isEmpty() ? options : partialMoves;
    }

    private Optional<MoveOption> planEntry(Piece piece, int roll, MysteryCell mysteryCell) {
        if (roll != ENTRY_ROLL || !piece.isInBase()) {
            return Optional.empty();
        }
        int startCell = piece.colour().startCell();
        if (isOpponentBlockAt(piece.colour(), startCell)) {
            return Optional.empty();
        }
        Route route = Route.completed(Position.base(), Position.onTrack(startCell), 0, 0);
        Landing landing = landingAt(startCell, List.of(piece), mysteryCell);
        return Optional.of(new MoveOption(Type.ENTER_BOARD, List.of(piece), route, landing, false));
    }

    private Optional<MoveOption> planPieceMove(Piece piece, int roll, MysteryCell mysteryCell) {
        boolean onTheWayHome = piece.isOnTrack() || piece.isInHomeStraight();
        int steps = piece.adjustRoll(roll);
        if (!onTheWayHome || !piece.canMove()) {
            return Optional.empty();
        }
        Walk walk = new Walk(Type.MOVE_PIECE, List.of(piece), piece.direction(), steps);
        return walk(walk).map(route -> toOption(walk, route, mysteryCell));
    }

    private List<MoveOption> planBlockMoves(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        List<MoveOption> options = new ArrayList<>();
        for (int cellIndex : ownBlockCells(pieces)) {
            List<Piece> block = furthestFromHomeFirst(board.occupantsAt(cellIndex));
            int distance = roll / block.size();
            boolean everyPieceCanMove = block.stream().allMatch(Piece::canMove);
            if (distance > 0 && everyPieceCanMove) {
                Walk walk = new Walk(Type.MOVE_BLOCK, block, block.get(0).direction(), distance);
                walk(walk).map(route -> toOption(walk, route, mysteryCell)).ifPresent(options::add);
            }
        }
        return options;
    }

    private Optional<Route> walk(Walk walk) {
        Position current = walk.start();
        int passesGained = 0;
        for (int step = 1; step <= walk.steps(); step++) {
            Optional<Position> next = nextPosition(walk, current, passesGained);
            if (next.isEmpty()) {
                return Optional.empty();
            }
            if (blocksPassage(walk, next.get(), step == walk.steps())) {
                Route partial = Route.completed(walk.start(), current, step - 1, passesGained);
                return stopBeforeBlock(walk, partial, next.get());
            }
            current = next.get();
            passesGained += arrivedAtApproach(current, walk.leader().colour()) ? 1 : 0;
        }
        return Optional.of(Route.completed(walk.start(), current, walk.steps(), passesGained));
    }

    private Optional<Position> nextPosition(Walk walk, Position current, int passesGained) {
        if (current.isHome()) {
            return Optional.empty();
        }
        if (current.isInHomeStraight()) {
            int nextStep = current.index() + 1;
            return Optional.of(nextStep < HOME_STRAIGHT_LENGTH ? Position.inHomeStraight(nextStep) : Position.home());
        }
        Piece leader = walk.leader();
        boolean atOwnApproach = current.index() == leader.colour().approachCell();
        if (walk.mayEnterHomeStraight() && atOwnApproach && isAllowedIntoHomeStraight(leader, passesGained)) {
            return Optional.of(Position.inHomeStraight(0));
        }
        return Optional.of(Position.onTrack(navigator.step(current.index(), walk.direction())));
    }

    private boolean isAllowedIntoHomeStraight(Piece piece, int passesGained) {
        int passes = piece.approachPasses() + passesGained;
        return piece.hasCaptured() && passes >= navigator.passesNeededToEnterHome(piece.direction());
    }

    private boolean blocksPassage(Walk walk, Position next, boolean isFinalStep) {
        if (!next.isOnTrack() || !isOpponentBlockAt(walk.leader().colour(), next.index())) {
            return false;
        }
        int movingGroupSize = walk.movers().size();
        boolean canCaptureBlock = isFinalStep && movingGroupSize > SINGLE_PIECE
                && board.occupantsAt(next.index()).size() == movingGroupSize;
        return !canCaptureBlock;
    }

    private Optional<Route> stopBeforeBlock(Walk walk, Route partial, Position blockCell) {
        Position intended = Position.onTrack(navigator.move(walk.start().index(), walk.steps(), walk.direction()));
        Piece blockingPiece = board.occupantsAt(blockCell.index()).get(0);
        return Optional.of(partial.stoppedBy(new Route.Blockage(intended, blockingPiece)));
    }

    private boolean arrivedAtApproach(Position position, Colour colour) {
        return position.isOnTrack() && position.index() == colour.approachCell();
    }

    private MoveOption toOption(Walk walk, Route route, MysteryCell mysteryCell) {
        boolean movesAtAll = route.distance() > 0;
        Landing landing = movesAtAll && route.destination().isOnTrack()
                ? landingAt(route.destination().index(), walk.movers(), mysteryCell)
                : Landing.offTrack();
        boolean leavesBlock = movesAtAll && walk.type() == Type.MOVE_PIECE && route.from().isOnTrack()
                && board.isBlockAt(route.from().index());
        return new MoveOption(walk.type(), walk.movers(), route, landing, leavesBlock);
    }

    private Landing landingAt(int cellIndex, List<Piece> movers, MysteryCell mysteryCell) {
        Colour colour = movers.get(0).colour();
        List<Piece> victims = findVictims(colour, cellIndex, movers.size());
        boolean formsBlock = board.occupantsAt(cellIndex).stream()
                .anyMatch(piece -> piece.colour() == colour && !movers.contains(piece));
        return new Landing(victims, formsBlock, mysteryCell.isAt(cellIndex));
    }

    private List<Piece> findVictims(Colour moverColour, int cellIndex, int movingGroupSize) {
        List<Piece> opponents = board.opponentsAt(cellIndex, moverColour);
        if (!board.isBlockAt(cellIndex)) {
            return opponents;
        }
        return opponents.size() == movingGroupSize ? opponents : List.of();
    }

    private boolean isOpponentBlockAt(Colour colour, int cellIndex) {
        return board.isBlockAt(cellIndex) && !board.isBlockOwnedBy(cellIndex, colour);
    }

    private Set<Integer> ownBlockCells(List<Piece> pieces) {
        Set<Integer> blockCells = new LinkedHashSet<>();
        for (Piece piece : pieces) {
            if (piece.isOnTrack() && board.isBlockOwnedBy(piece.position().index(), piece.colour())) {
                blockCells.add(piece.position().index());
            }
        }
        return blockCells;
    }

    private List<Piece> furthestFromHomeFirst(List<Piece> block) {
        Piece furthestFromHome = block.get(0);
        for (Piece piece : block) {
            if (navigator.stepsToHome(piece) > navigator.stepsToHome(furthestFromHome)) {
                furthestFromHome = piece;
            }
        }
        List<Piece> ordered = new ArrayList<>();
        ordered.add(furthestFromHome);
        for (Piece piece : block) {
            if (piece != furthestFromHome) {
                ordered.add(piece);
            }
        }
        return ordered;
    }

    private record Walk(Type type, List<Piece> movers, Direction direction, int steps) {

        Piece leader() {
            return movers.get(0);
        }

        Position start() {
            return leader().position();
        }

        boolean mayEnterHomeStraight() {
            return type == Type.MOVE_PIECE;
        }
    }
}
