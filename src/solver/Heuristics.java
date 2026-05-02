package solver;

import model.Board;
import model.Position;
import model.Puzzle;

import java.util.List;

public final class Heuristics {
    private Heuristics() {
    }

    public static Heuristic create(HeuristicType type, Puzzle puzzle) {
        final int minPassableTileCost = findMinPassableTileCost(puzzle);

        if (type == HeuristicType.H1) {
            return new BasicHeuristic(type, puzzle, minPassableTileCost) {
                @Override
                public int estimate(Puzzle puzzle, SearchState state) {
                    return manhattan(state.getActorPosition(), puzzle.getGoalPosition()) * minPassableTileCost;
                }
            };
        }
        if (type == HeuristicType.H2) {
            return new BasicHeuristic(type, puzzle, minPassableTileCost) {
                @Override
                public int estimate(Puzzle puzzle, SearchState state) {
                    Position target = nextMandatoryTarget(puzzle, state);
                    return manhattan(state.getActorPosition(), target) * minPassableTileCost;
                }
            };
        }
        if (type == HeuristicType.H3) {
            return new BasicHeuristic(type, puzzle, minPassableTileCost) {
                @Override
                public int estimate(Puzzle puzzle, SearchState state) {
                    List<Position> checkpoints = puzzle.getCheckpointPositions();
                    Position current = state.getActorPosition();
                    int distance = 0;

                    for (int index = state.getNextCheckpointIndex(); index < checkpoints.size(); index++) {
                        Position checkpoint = checkpoints.get(index);
                        distance += manhattan(current, checkpoint);
                        current = checkpoint;
                    }

                    distance += manhattan(current, puzzle.getGoalPosition());
                    return distance * minPassableTileCost;
                }
            };
        }

        throw new IllegalArgumentException("Unsupported heuristic type: " + type);
    }

    public static String describe(HeuristicType type) {
        if (type == HeuristicType.H1) {
            return "H1: Manhattan(current, goal) * minPassableTileCost; ignores remaining checkpoints.";
        }
        if (type == HeuristicType.H2) {
            return "H2: Manhattan(current, next mandatory target) * minPassableTileCost; useful for checkpoint order.";
        }
        if (type == HeuristicType.H3) {
            return "H3: Manhattan chain through remaining checkpoints then goal * minPassableTileCost; experimental, not claimed universally admissible.";
        }
        return "";
    }

    private static Position nextMandatoryTarget(Puzzle puzzle, SearchState state) {
        List<Position> checkpoints = puzzle.getCheckpointPositions();
        if (state.getNextCheckpointIndex() < checkpoints.size()) {
            return checkpoints.get(state.getNextCheckpointIndex());
        }
        return puzzle.getGoalPosition();
    }

    private static int findMinPassableTileCost(Puzzle puzzle) {
        Board board = puzzle.getBoard();
        int minCost = Integer.MAX_VALUE;
        for (int row = 0; row < board.getRowCount(); row++) {
            for (int col = 0; col < board.getColCount(); col++) {
                Position position = new Position(row, col);
                if (!board.isObstacle(position) && !board.isLava(position)) {
                    minCost = Math.min(minCost, board.getCost(position));
                }
            }
        }
        return minCost;
    }

    private static int manhattan(Position a, Position b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }

    private abstract static class BasicHeuristic implements Heuristic {
        private final HeuristicType type;

        private BasicHeuristic(HeuristicType type, Puzzle puzzle, int minPassableTileCost) {
            this.type = type;
            if (minPassableTileCost <= 0) {
                throw new IllegalArgumentException("Puzzle must have at least one passable tile with positive cost.");
            }
        }

        @Override
        public HeuristicType getType() {
            return type;
        }
    }
}
