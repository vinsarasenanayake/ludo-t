package com.ludot.rules;

import com.ludot.domain.Blockage;
import com.ludot.domain.Board;
import com.ludot.domain.Cell;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;
import com.ludot.domain.Route;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ludot.domain.BoardConstants.ENTRY_ROLL;
import static com.ludot.domain.BoardConstants.HOME_STRAIGHT_LENGTH;

// Works out every legal move for a roll. It only plans: nothing on the board changes here.
public class MovePlanner {

    private static final int SINGLE_PIECE = 1;

    private final Board board;
    private final TrackNavigator navigator;
    private final CaptureResolver captureResolver;

    public MovePlanner(Board board, TrackNavigator navigator, CaptureResolver captureResolver) {
        this.board = board;
        this.navigator = navigator;
        this.captureResolver = captureResolver;
    }

    public List<MoveOption> findOptions(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        List<MoveOption> options = new ArrayList<>();
        planEntry(pieces, roll, mysteryCell).ifPresent(options::add);
        for (Piece piece : pieces) {
            planPieceMove(piece, roll, mysteryCell).ifPresent(options::add);
        }
        options.addAll(planBlockMoves(pieces, roll, mysteryCell));
        return options;
    }

    private Optional<MoveOption> planEntry(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        Optional<Piece> waitingPiece = pieces.stream().filter(Piece::isInBase).findFirst();
        if (roll != ENTRY_ROLL || waitingPiece.isEmpty()) {
            return Optional.empty();
        }
        Piece piece = waitingPiece.get();
        int startCell = piece.colour().startCell();
        if (isOpponentBlockAt(piece.colour(), startCell)) {
            return Optional.empty();
        }
        Route route = Route.completed(Position.base(), Position.onTrack(startCell), 0, 0);
        Landing landing = landingAt(startCell, List.of(piece), mysteryCell);
        return Optional.of(new MoveOption(MoveType.ENTER_BOARD, List.of(piece), route, landing, false));
    }

    private Optional<MoveOption> planPieceMove(Piece piece, int roll, MysteryCell mysteryCell) {
        boolean onTheWayHome = piece.isOnTrack() || piece.isInHomeStraight();
        int steps = piece.adjustRoll(roll);
        if (!onTheWayHome || !piece.canMove() || steps == 0) {
            return Optional.empty();
        }
        Walk walk = new Walk(MoveType.MOVE_PIECE, List.of(piece), piece.direction(), steps);
        return walk(walk).map(route -> toOption(walk, route, mysteryCell));
    }

    // Rule T-4: a block moves roll / (pieces in the block) cells, in the direction of the piece furthest from home.
    private List<MoveOption> planBlockMoves(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        List<MoveOption> options = new ArrayList<>();
        for (int cellIndex : ownBlockCells(pieces)) {
            List<Piece> block = board.occupantsAt(cellIndex);
            int distance = roll / block.size();
            boolean everyPieceCanMove = block.stream().allMatch(Piece::canMove);
            if (distance > 0 && everyPieceCanMove) {
                Walk walk = new Walk(MoveType.MOVE_BLOCK, block, blockDirection(block), distance);
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

    // Rules T-1 and T-7: the piece needs a capture, and enough passes of its approach for its direction.
    private boolean isAllowedIntoHomeStraight(Piece piece, int passesGained) {
        int passes = piece.approachPasses() + passesGained;
        return piece.hasCaptured() && passes >= navigator.passesNeededToEnterHome(piece.direction());
    }

    // Rule T-3: nobody passes an opponent block. Rule T-8: a same-size block may land on it and capture it.
    private boolean blocksPassage(Walk walk, Position next, boolean isFinalStep) {
        if (!next.isOnTrack() || !isOpponentBlockAt(walk.leader().colour(), next.index())) {
            return false;
        }
        int movingGroupSize = walk.movers().size();
        boolean canCaptureBlock = isFinalStep && movingGroupSize > SINGLE_PIECE
                && board.occupantsAt(next.index()).size() == movingGroupSize;
        return !canCaptureBlock;
    }

    // Rule T-3: stop on the cell before the block, as long as the piece moved at least one cell.
    private Optional<Route> stopBeforeBlock(Walk walk, Route partial, Position blockCell) {
        if (partial.distance() == 0) {
            return Optional.empty();
        }
        return Optional.of(partial.stoppedBy(blockageAt(walk, blockCell)));
    }

    private Blockage blockageAt(Walk walk, Position blockCell) {
        Position intended = Position.onTrack(navigator.move(walk.start().index(), walk.steps(), walk.direction()));
        Piece blockingPiece = board.occupantsAt(blockCell.index()).get(0);
        return new Blockage(intended, blockingPiece);
    }

    private boolean arrivedAtApproach(Position position, Colour colour) {
        return position.isOnTrack() && position.index() == colour.approachCell();
    }

    private MoveOption toOption(Walk walk, Route route, MysteryCell mysteryCell) {
        Landing landing = route.destination().isOnTrack()
                ? landingAt(route.destination().index(), walk.movers(), mysteryCell)
                : Landing.offTrack();
        boolean leavesBlock = walk.type() == MoveType.MOVE_PIECE && board.isBlockAt(route.from().index());
        return new MoveOption(walk.type(), walk.movers(), route, landing, leavesBlock);
    }

    private Landing landingAt(int cellIndex, List<Piece> movers, MysteryCell mysteryCell) {
        Colour colour = movers.get(0).colour();
        List<Piece> victims = captureResolver.findVictims(colour, cellIndex, movers.size());
        boolean formsBlock = board.occupantsAt(cellIndex).stream()
                .anyMatch(piece -> piece.colour() == colour && !movers.contains(piece));
        return new Landing(victims, formsBlock, mysteryCell.isAt(cellIndex));
    }

    private boolean isOpponentBlockAt(Colour colour, int cellIndex) {
        Cell cell = board.cellAt(cellIndex);
        return cell.isBlock() && !cell.isBlockOwnedBy(colour);
    }

    private Set<Integer> ownBlockCells(List<Piece> pieces) {
        Set<Integer> blockCells = new LinkedHashSet<>();
        for (Piece piece : pieces) {
            if (piece.isOnTrack() && board.cellAt(piece.position().index()).isBlockOwnedBy(piece.colour())) {
                blockCells.add(piece.position().index());
            }
        }
        return blockCells;
    }

    private Direction blockDirection(List<Piece> block) {
        Piece furthestFromHome = block.get(0);
        for (Piece piece : block) {
            if (navigator.stepsToHome(piece) > navigator.stepsToHome(furthestFromHome)) {
                furthestFromHome = piece;
            }
        }
        return furthestFromHome.direction();
    }

    // Parameter object: everything needed to walk a piece (or a block) cell by cell.
    private record Walk(MoveType type, List<Piece> movers, Direction direction, int steps) {

        Piece leader() {
            return movers.get(0);
        }

        Position start() {
            return leader().position();
        }

        // Assumption: only a single piece may turn into its home straight; a block stays on the track.
        boolean mayEnterHomeStraight() {
            return type == MoveType.MOVE_PIECE;
        }
    }
}
