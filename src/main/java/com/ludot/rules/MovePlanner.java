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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ludot.board.BoardConstants.ENTRY_ROLL;
import static com.ludot.board.BoardConstants.HOME_STRAIGHT_LENGTH;

// Finds every legal move for a roll
public final class MovePlanner {

    private static final int SINGLE_PIECE = 1;
    private static final int NO_STEPS = 0;

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
            planPieceMove(piece, piece.adjustRoll(roll), mysteryCell).ifPresent(options::add);
        }
        options.addAll(planBlockMoves(pieces, roll, mysteryCell));
        return withoutBlockedMovesIfOthersExist(options);
    }

    public Optional<MoveOption> planPieceMove(Piece piece, int steps, MysteryCell mysteryCell) {
        boolean inPlay = piece.isOnTrack() || piece.isInHomeStraight();
        if (!inPlay || !piece.canMove()) {
            return Optional.empty();
        }
        Walk walk = new Walk(Type.MOVE_PIECE, List.of(piece), piece.direction(), steps);
        return traceRoute(walk).map(route -> toOption(walk, route, mysteryCell));
    }

    // T-3: a move cut short by a block is a last resort
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
        // T-3: an opponent block on X stops entry
        if (board.isOpponentBlockAt(startCell, piece.colour())) {
            return Optional.of(blockedEntry(piece, startCell));
        }
        Route route = Route.completed(Position.base(), Position.onTrack(startCell), NO_STEPS, 0);
        Landing landing = landingAt(startCell, List.of(piece), mysteryCell);
        return Optional.of(new MoveOption(Type.ENTER_BOARD, List.of(piece), route, landing, false));
    }

    private MoveOption blockedEntry(Piece piece, int startCell) {
        Piece blocker = blockingPieces(startCell, piece.colour()).getFirst();
        Route route = Route.completed(Position.base(), Position.base(), NO_STEPS, 0)
                .stoppedBy(new Route.Blockage(Position.onTrack(startCell), blocker));
        return new MoveOption(Type.ENTER_BOARD, List.of(piece), route, Landing.offTrack(), false);
    }

    private List<MoveOption> planBlockMoves(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        List<MoveOption> options = new ArrayList<>();
        for (int cellIndex : ownBlockCells(pieces)) {
            List<Piece> block = furthestFromHomeFirst(ownPiecesAt(cellIndex, pieces));
            Piece leader = block.getFirst();
            int distance = roll / block.size();
            boolean everyPieceCanMove = block.stream().allMatch(Piece::canMove);
            if (distance > 0 && everyPieceCanMove) {
                Walk walk = new Walk(Type.MOVE_BLOCK, block, leader.direction(), distance);
                traceRoute(walk).map(route -> toOption(walk, route, mysteryCell)).ifPresent(options::add);
            }
        }
        return options;
    }

    // Follows the full path, then stops at the first block
    private Optional<Route> traceRoute(Walk walk) {
        List<Position> path = pathIgnoringBlocks(walk);
        if (path.isEmpty()) {
            return Optional.empty();
        }
        for (int step = 1; step <= path.size(); step++) {
            if (blocksPassage(walk, path.get(step - 1), step == path.size())) {
                return Optional.of(stopBeforeBlock(walk, path, step));
            }
        }
        return Optional.of(routeAlong(walk, path, path.size()));
    }

    private List<Position> pathIgnoringBlocks(Walk walk) {
        List<Position> path = new ArrayList<>();
        Position current = walk.start();
        int passesGained = 0;
        for (int step = 0; step < walk.steps(); step++) {
            Optional<Position> next = nextPosition(walk, current, passesGained);
            if (next.isEmpty()) {
                return List.of();
            }
            current = next.get();
            path.add(current);
            passesGained += arrivedAtApproach(current, walk.leader().colour()) ? 1 : 0;
        }
        return path;
    }

    private Route routeAlong(Walk walk, List<Position> path, int steps) {
        Position end = steps == NO_STEPS ? walk.start() : path.get(steps - 1);
        int passesGained = (int) path.subList(0, steps).stream()
                .filter(position -> arrivedAtApproach(position, walk.leader().colour()))
                .count();
        return Route.completed(walk.start(), end, steps, passesGained);
    }

    private Route stopBeforeBlock(Walk walk, List<Position> path, int blockStep) {
        // Landing on a block is blocked
        boolean wouldLandOnBlock = blockStep == path.size();
        int stepsTaken = wouldLandOnBlock ? NO_STEPS : blockStep - 1;
        Position blockCell = path.get(blockStep - 1);
        Piece blockingPiece = blockingPieces(blockCell.index(), walk.leader().colour()).getFirst();
        Position intended = path.getLast();
        return routeAlong(walk, path, stepsTaken).stoppedBy(new Route.Blockage(intended, blockingPiece));
    }

    private Optional<Position> nextPosition(Walk walk, Position current, int passesGained) {
        if (current.isHome()) {
            return Optional.empty();
        }
        if (current.isInHomeStraight()) {
            int nextStep = current.index() + 1;
            return Optional.of(nextStep < HOME_STRAIGHT_LENGTH ? Position.inHomeStraight(nextStep) : Position.home());
        }
        // R9 + T-7: turn home only if every mover may
        boolean atOwnApproach = current.index() == walk.leader().colour().approachCell();
        if (atOwnApproach && everyMoverMayEnterHomeStraight(walk, passesGained)) {
            return Optional.of(Position.inHomeStraight(0));
        }
        return Optional.of(Position.onTrack(navigator.step(current.index(), walk.direction())));
    }

    private boolean everyMoverMayEnterHomeStraight(Walk walk, int passesGained) {
        return walk.movers().stream()
                .allMatch(mover -> isAllowedIntoHomeStraight(mover, walk.passesCreditedTo(mover, passesGained)));
    }

    // T-7 + T-1: needs a capture and enough approach passes
    private boolean isAllowedIntoHomeStraight(Piece piece, int passesGained) {
        int passes = piece.approachPasses() + passesGained;
        return piece.hasCaptured() && passes >= navigator.passesNeededToEnterHome(piece.direction());
    }

    private boolean blocksPassage(Walk walk, Position next, boolean isFinalStep) {
        Colour moverColour = walk.leader().colour();
        if (!next.isOnTrack() || !board.isOpponentBlockAt(next.index(), moverColour)) {
            return false;
        }
        int movingGroupSize = walk.movers().size();
        // T-8: only an equal block can capture a block
        boolean canCaptureBlock = isFinalStep && movingGroupSize > SINGLE_PIECE
                && blockingPieces(next.index(), moverColour).size() == movingGroupSize;
        return !canCaptureBlock;
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
                && board.isBlockOwnedBy(route.from().index(), walk.leader().colour());
        return new MoveOption(walk.type(), walk.movers(), route, landing, leavesBlock);
    }

    private Landing landingAt(int cellIndex, List<Piece> movers, MysteryCell mysteryCell) {
        Colour colour = movers.getFirst().colour();
        List<Piece> victims = findVictims(colour, cellIndex, movers.size());
        boolean formsBlock = board.occupantsAt(cellIndex).stream()
                .anyMatch(piece -> piece.colour() == colour && !movers.contains(piece));
        return new Landing(victims, formsBlock, mysteryCell.isAt(cellIndex));
    }

    private List<Piece> findVictims(Colour moverColour, int cellIndex, int movingGroupSize) {
        List<Piece> opponents = board.opponentsAt(cellIndex, moverColour);
        if (!board.isOpponentBlockAt(cellIndex, moverColour)) {
            return opponents;
        }
        return blockingPieces(cellIndex, moverColour).size() == movingGroupSize ? opponents : List.of();
    }

    private List<Piece> blockingPieces(int cellIndex, Colour moverColour) {
        return board.opponentsAt(cellIndex, moverColour).stream()
                .filter(piece -> board.isBlockOwnedBy(cellIndex, piece.colour()))
                .toList();
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

    private List<Piece> ownPiecesAt(int cellIndex, List<Piece> pieces) {
        return board.occupantsAt(cellIndex).stream()
                .filter(pieces::contains)
                .toList();
    }

    private List<Piece> furthestFromHomeFirst(List<Piece> block) {
        Piece furthestFromHome = Collections.max(block, Comparator.comparingInt(navigator::stepsToHome));
        List<Piece> ordered = new ArrayList<>(block);
        ordered.remove(furthestFromHome);
        ordered.addFirst(furthestFromHome);
        return ordered;
    }

    // One planned walk: who moves, which way, and how far
    private record Walk(Type type, List<Piece> movers, Direction direction, int steps) {

        Piece leader() {
            return movers.getFirst();
        }

        Position start() {
            return leader().position();
        }

        int passesCreditedTo(Piece mover, int passesGained) {
            return mover.direction() == direction ? passesGained : 0;
        }
    }
}