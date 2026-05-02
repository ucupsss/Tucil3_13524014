import model.Position;
import model.Puzzle;
import parser.InvalidPuzzleException;
import parser.PuzzleParser;
import solver.GreedyBestFirstSolver;
import solver.Heuristic;
import solver.HeuristicType;
import solver.Heuristics;
import solver.SearchAlgorithm;
import solver.SearchNode;
import solver.SearchResult;
import solver.SearchState;
import solver.UniformCostSolver;

import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputPath = promptForInputPath(args, scanner);

        try {
            Puzzle puzzle = new PuzzleParser().parse(Paths.get(inputPath));
            SearchAlgorithm algorithm = promptForAlgorithm(scanner);
            HeuristicType heuristicType = null;
            if (algorithm == SearchAlgorithm.GBFS) {
                heuristicType = promptForHeuristic(scanner);
            }

            System.out.println("Input parsed successfully.");
            System.out.println();
            System.out.println(renderState(puzzle, new SearchState(puzzle.getStartPosition(), 0)));
            System.out.println();
            System.out.println("Start: " + puzzle.getStartPosition());
            System.out.println("Goal: " + puzzle.getGoalPosition());
            System.out.println("Checkpoints: " + formatPositions(puzzle.getCheckpointPositions()));
            System.out.println("Cost matrix: " + summarizeCosts(puzzle));
            System.out.println();

            SearchResult result = runSearch(puzzle, algorithm, heuristicType);
            printSearchResult(puzzle, result, algorithm, heuristicType);
        } catch (InvalidPuzzleException exception) {
            System.err.println("Invalid input: " + exception.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid option: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static String promptForInputPath(String[] args, Scanner scanner) {
        if (args.length > 0) {
            return args[0];
        }
        System.out.print("Masukan file input: ");
        return scanner.nextLine().trim();
    }

    private static SearchAlgorithm promptForAlgorithm(Scanner scanner) {
        System.out.print("Pilih algoritma (UCS/GBFS): ");
        return SearchAlgorithm.fromInput(scanner.nextLine());
    }

    private static HeuristicType promptForHeuristic(Scanner scanner) {
        System.out.print("Pilih heuristic (H0/H1/H2/H3): ");
        return HeuristicType.fromInput(scanner.nextLine());
    }

    private static String formatPositions(List<Position> positions) {
        if (positions.isEmpty()) {
            return "none";
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < positions.size(); index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(index).append("=").append(positions.get(index));
        }
        return builder.toString();
    }

    private static String summarizeCosts(Puzzle puzzle) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long total = 0;

        for (int row = 0; row < puzzle.getRowCount(); row++) {
            for (int col = 0; col < puzzle.getColCount(); col++) {
                int cost = puzzle.getBoard().getCost(new Position(row, col));
                min = Math.min(min, cost);
                max = Math.max(max, cost);
                total += cost;
            }
        }

        return puzzle.getRowCount() + "x" + puzzle.getColCount()
                + ", min=" + min
                + ", max=" + max
                + ", total=" + total;
    }

    private static SearchResult runSearch(Puzzle puzzle, SearchAlgorithm algorithm, HeuristicType heuristicType) {
        if (algorithm == SearchAlgorithm.UCS) {
            return new UniformCostSolver().solve(puzzle);
        }

        Heuristic heuristic = Heuristics.create(heuristicType, puzzle);
        return new GreedyBestFirstSolver(heuristic).solve(puzzle);
    }

    private static void printSearchResult(
            Puzzle puzzle,
            SearchResult result,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType
    ) {
        System.out.println("Algorithm: " + algorithm);
        if (algorithm == SearchAlgorithm.GBFS) {
            System.out.println("Heuristic: " + heuristicType + " - " + Heuristics.describe(heuristicType));
            System.out.println("Note: GBFS uses heuristics only to guide search; optimality is not guaranteed.");
            System.out.println("Tie-break order: generated moves U, D, L, R; queue ties use lower h/f, lower g, then insertion order.");
        } else {
            System.out.println("Tie-break order: generated moves U, D, L, R; queue ties use lower g, then insertion order.");
        }
        System.out.println("Solution found: " + (result.isFound() ? "yes" : "no"));
        System.out.println("Solution string: " + (result.isFound() ? result.getSolutionMoves() : "-"));
        System.out.println("Cost: " + (result.isFound() ? result.getTotalCost() : "-"));
        System.out.println("Execution time: " + result.getExecutionTimeMillis() + " ms");
        System.out.println("Iterations: " + result.getIterationCount());

        if (!result.isFound()) {
            return;
        }

        List<SearchNode> path = result.getSolutionPath();
        System.out.println();
        System.out.println("Initial");
        System.out.println(renderState(puzzle, path.get(0).getState()));

        for (int index = 1; index < path.size(); index++) {
            SearchNode node = path.get(index);
            System.out.println();
            System.out.println("Step " + index + " : " + node.getMoveFromParent().getCode());
            System.out.println(renderState(puzzle, node.getState()));
        }
    }

    private static String renderState(Puzzle puzzle, SearchState state) {
        StringBuilder builder = new StringBuilder();
        Position actorPosition = state.getActorPosition();
        int completedCheckpointCount = state.getNextCheckpointIndex();

        for (int row = 0; row < puzzle.getRowCount(); row++) {
            if (row > 0) {
                builder.append(System.lineSeparator());
            }
            for (int col = 0; col < puzzle.getColCount(); col++) {
                Position position = new Position(row, col);
                if (position.equals(actorPosition)) {
                    builder.append('Z');
                } else if (position.equals(puzzle.getStartPosition())) {
                    builder.append('*');
                } else if (isCompletedCheckpoint(puzzle, position, completedCheckpointCount)) {
                    builder.append('*');
                } else {
                    builder.append(puzzle.getBoard().getTile(position));
                }
            }
        }

        return builder.toString();
    }

    private static boolean isCompletedCheckpoint(Puzzle puzzle, Position position, int completedCheckpointCount) {
        for (int index = 0; index < completedCheckpointCount; index++) {
            if (puzzle.getCheckpointPositions().get(index).equals(position)) {
                return true;
            }
        }
        return false;
    }
}
