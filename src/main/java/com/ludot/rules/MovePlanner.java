package com.ludot.rules;

import com.ludot.domain.Board;
import com.ludot.domain.Cell;
import com.ludot.domain.Colour;
import com.ludot.domain.Direction;
import com.ludot.domain.MysteryCell;
import com.ludot.domain.Piece;
import com.ludot.domain.Position;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ludot.domain.BoardConstants.ENTRY_ROLL;
import static com.ludot.domain.BoardConstants.HOME_STRAIGHT_LENGTH;

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
        Optional<Route> route = walk(List.of(piece), piece.direction(), steps, true);
        return route.map(found -> toOption(MoveType.MOVE_PIECE, List.of(piece), found, mysteryCell));
    }

    private List<MoveOption> planBlockMoves(List<Piece> pieces, int roll, MysteryCell mysteryCell) {
        List<MoveOption> options = new ArrayList<>();
        for (int cellIndex : ownBlockCells(pieces)) {
            List<Piece> block = board.occupantsAt(cellIndex);
            int distance = roll / block.size();
            boolean everyPieceCanMove = block.stream().allMatch(Piece::canMove);
            if (distance > 0 && everyPieceCanMove) {
                walk(block, blockDirection(block), distance, false)
                        .map(route -> toOption(MoveType.MOVE_BLOCK, block, route, mysteryCell))
                        .ifPresent(options::add);
            }
        }
        return options;
    }

    private Optional<Route> walk(List<Piece> movers, Direction direction, int steps, boolean mayEnterHomeStraight) {
        Piece leader = movers.get(0);
        Position start = leader.position();
        Position current = start;
        int passesGained = 0;
        for (int step = 1; step <= steps; step++) {
            Optional<Position> next = nextPosition(leader, current, direction, passesGained, mayEnterHomeStraight);
            if (next.isEmpty()) {
                return Optional.empty();
            }
            if (blocksPassage(next.get(), leader.colour(), movers.size(), step == steps)) {
                return cutShortRoute(start, current, step - 1, passesGained, blockageAt(start, next.get(), steps, direction));
            }
            current = next.get();
            passesGained += arrivedAtApproach(current, leader.colour()) ? 1 : 0;
        }
        return Optional.of(Route.completed(start, current, steps, passesGained));
    }

    private Optional<Position> nextPosition(Piece leader, Position current, Direction direction,
                                            int passesGained, boolean mayEnterHomeStraight) {
        if (current.isHome()) {
            return Optional.empty();
        }
        if (current.isInHomeStraight()) {
            int nextStep = current.index() + 1;
            return Optional.of(nextStep < HOME_STRAIGHT_LENGTH ? Position.inHomeStraight(nextStep) : Position.home());
        }
        boolean atOwnApproach = current.index() == leader.colour().approachCell();
        if (mayEnterHomeStraight && atOwnApproach && isAllowedIntoHomeStraight(leader, passesGained)) {
            return Optional.of(Position.inHomeStraight(0));
        }
        return Optional.of(Position.onTrack(navigator.step(current.index(), direction)));
    }

    private boolean isAllowedIntoHomeStraight(Piece piece, int passesGained) {
        int passes = piece.approachPasses() + passesGained;
        return piece.hasCaptured() && passes >= navigator.passesNeededToEnterHome(piece.direction());
    }

    private boolean blocksPassage(Position next, Colour moverColour, int movingGroupSize, boolean isFinalStep) {
        if (!next.isOnTrack() || !isOpponentBlockAt(moverColour, next.index())) {
            return false;
        }
        boolean canCaptureBlock = isFinalStep && movingGroupSize > SINGLE_PIECE
                && board.occupantsAt(next.index()).size() == movingGroupSize;
        return !canCaptureBlock;
    }

    private Optional<Route> cutShortRoute(Position start, Position stoppedAt, int stepsTaken,
                                          int passesGained, Blockage blockage) {
        if (stepsTaken == 0) {
            return Optional.empty();
        }
        return Optional.of(Route.cutShort(start, stoppedAt, stepsTaken, passesGained, blockage));
    }

    private Blockage blockageAt(Position start, Position blockCell, int steps, Direction direction) {
        Position intended = Position.onTrack(navigator.move(start.index(), steps, direction));
        Piece blockingPiece = board.occupantsAt(blockCell.index()).get(0);
        return new Blockage(intended, blockingPiece);
    }

    private boolean arrivedAtApproach(Position position, Colour colour) {
        return position.isOnTrack() && position.index() == colour.approachCell();
    }

    private MoveOption toOption(MoveType type, List<Piece> movers, Route route, MysteryCell mysteryCell) {
        Landing landing = route.destination().isOnTrack()
                ? landingAt(route.destination().index(), movers, mysteryCell)
                : Landing.offTrack();
        boolean leavesBlock = type == MoveType.MOVE_PIECE && board.isBlockAt(route.from().index());
        return new MoveOption(type, movers, route, landing, leavesBlock);
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
}