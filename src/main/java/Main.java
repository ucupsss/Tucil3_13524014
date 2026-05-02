import cli.PlaybackController;
import model.Position;
import model.Puzzle;
import parser.InvalidPuzzleException;
import parser.PuzzleParser;
import solver.AStarSolver;
import solver.GreedyBestFirstSolver;
import solver.Heuristic;
import solver.HeuristicType;
import solver.Heuristics;
import solver.SearchAlgorithm;
import solver.SearchNode;
import solver.SearchResult;
import solver.SearchState;
import solver.UniformCostSolver;
import util.BoardRenderer;
import util.SolutionWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputPath = promptForInputPath(args, scanner);
        Path inputFilePath = Paths.get(inputPath);

        try {
            Puzzle puzzle = new PuzzleParser().parse(inputFilePath);
            SearchAlgorithm algorithm = promptForAlgorithm(scanner);
            HeuristicType heuristicType = null;
            if (usesHeuristic(algorithm)) {
                heuristicType = promptForHeuristic(scanner);
            }

            System.out.println("Input parsed successfully.");
            System.out.println();
            System.out.println(BoardRenderer.renderState(puzzle, new SearchState(puzzle.getStartPosition(), 0)));
            System.out.println();
            System.out.println("Start: " + puzzle.getStartPosition());
            System.out.println("Goal: " + puzzle.getGoalPosition());
            System.out.println("Checkpoints: " + formatPositions(puzzle.getCheckpointPositions()));
            System.out.println("Cost matrix: " + summarizeCosts(puzzle));
            System.out.println();

            SearchResult result = runSearch(puzzle, algorithm, heuristicType);
            printSearchResult(puzzle, result, algorithm, heuristicType);
            offerPlayback(scanner, puzzle, result);
            offerSave(scanner, inputFilePath, puzzle, algorithm, heuristicType, result);
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
        System.out.print("Pilih algoritma (UCS/GBFS/A*): ");
        return SearchAlgorithm.fromInput(scanner.nextLine());
    }

    private static HeuristicType promptForHeuristic(Scanner scanner) {
        System.out.print("Pilih heuristic (H1/H2/H3): ");
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
        if (algorithm == SearchAlgorithm.GBFS) {
            return new GreedyBestFirstSolver(heuristic).solve(puzzle);
        }
        return new AStarSolver(heuristic).solve(puzzle);
    }

    private static boolean usesHeuristic(SearchAlgorithm algorithm) {
        return algorithm == SearchAlgorithm.GBFS || algorithm == SearchAlgorithm.ASTAR;
    }

    private static void printSearchResult(
            Puzzle puzzle,
            SearchResult result,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType
    ) {
        System.out.println("Algorithm: " + algorithm.getDisplayName());
        if (algorithm == SearchAlgorithm.GBFS) {
            System.out.println("Heuristic: " + heuristicType + " - " + Heuristics.describe(heuristicType));
            System.out.println("Note: GBFS uses heuristics only to guide search; optimality is not guaranteed.");
            System.out.println("Evaluation: f(n) = h(n), where h(n) is estimated remaining cost.");
            System.out.println("Tie-break order: generated moves U, D, L, R; queue ties use lower h/f, lower g, then insertion order.");
        } else if (algorithm == SearchAlgorithm.ASTAR) {
            System.out.println("Heuristic: " + heuristicType + " - " + Heuristics.describe(heuristicType));
            System.out.println("Evaluation: f(n) = g(n) + h(n), where g(n) is accumulated movement cost.");
            System.out.println("Note: A* is guaranteed optimal only with an admissible heuristic and correct repeated-state handling.");
            System.out.println("Note: H1/H2/H3 are not claimed universally admissible for every ice sliding case.");
            System.out.println("Tie-break order: generated moves U, D, L, R; queue ties use lower f, lower h, lower g, then insertion order.");
        } else {
            System.out.println("Evaluation: f(n) = g(n), where g(n) is accumulated movement cost.");
            System.out.println("Tie-break order: generated moves U, D, L, R; queue ties use lower g, then insertion order.");
        }
        System.out.println("Solusi Yang Ditemukan : " + (result.isFound() ? result.getSolutionMoves() : "-"));
        System.out.println("Cost dari Solusi : " + (result.isFound() ? result.getTotalCost() : "-"));

        if (!result.isFound()) {
            System.out.println("Waktu eksekusi: " + result.getExecutionTimeMillis() + " ms");
            System.out.println("Banyak iterasi yang dilakukan: " + result.getIterationCount() + " iterasi");
            return;
        }

        List<SearchNode> path = result.getSolutionPath();
        System.out.println();
        System.out.println("Initial");
        System.out.println(BoardRenderer.renderState(puzzle, path.get(0).getState()));

        for (int index = 1; index < path.size(); index++) {
            SearchNode node = path.get(index);
            System.out.println();
            System.out.println("Step " + index + " : " + node.getMoveFromParent().getCode());
            System.out.println(BoardRenderer.renderState(puzzle, node.getState()));
        }

        System.out.println();
        System.out.println("Waktu eksekusi: " + result.getExecutionTimeMillis() + " ms");
        System.out.println("Banyak iterasi yang dilakukan: " + result.getIterationCount() + " iterasi");
    }

    private static void offerPlayback(Scanner scanner, Puzzle puzzle, SearchResult result) {
        if (!result.isFound()) {
            return;
        }
        if (askYesNo(scanner, "Apakah Anda ingin melakukan playback? (Ya/Tidak): ")) {
            new PlaybackController(puzzle, result, scanner).run();
        }
    }

    private static void offerSave(
            Scanner scanner,
            Path inputPath,
            Puzzle puzzle,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType,
            SearchResult result
    ) {
        if (!askYesNo(scanner, "Apakah Anda ingin menyimpan solusi? (Ya/Tidak): ")) {
            return;
        }

        System.out.print("Masukan path output .txt, kosongkan untuk default di folder test/: ");
        String output = scanner.nextLine().trim();
        Path solutionPath = output.isEmpty()
                ? defaultSolutionPath(inputPath, algorithm, heuristicType)
                : Paths.get(output);
        Path iterationPath = iterationPathFor(solutionPath);

        SolutionWriter writer = new SolutionWriter();
        try {
            writer.writeSolution(solutionPath, inputPath, puzzle, algorithm, heuristicType, result);
            writer.writeExploredSnapshots(iterationPath, inputPath, puzzle, algorithm, heuristicType, result);
            System.out.println("Solusi disimpan pada " + solutionPath);
            System.out.println("Snapshot iterasi disimpan pada " + iterationPath);
        } catch (IOException exception) {
            System.out.println("Gagal menyimpan file: " + exception.getMessage());
        }
    }

    private static boolean askYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("ya") || answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("tidak") || answer.equals("t") || answer.equals("no") || answer.equals("n")) {
                return false;
            }
            System.out.println("Input tidak valid. Jawab Ya atau Tidak.");
        }
    }

    private static Path defaultSolutionPath(Path inputPath, SearchAlgorithm algorithm, HeuristicType heuristicType) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(stripExtension(inputPath.getFileName().toString()))
                .append("_")
                .append(algorithm.getDisplayName().replace("*", "star"));
        if (heuristicType != null) {
            fileName.append("_").append(heuristicType);
        }
        fileName.append("_solution.txt");
        return Paths.get("test", fileName.toString());
    }

    private static Path iterationPathFor(Path solutionPath) {
        String fileName = solutionPath.getFileName().toString();
        String base = stripExtension(fileName);
        Path parent = solutionPath.getParent();
        if (parent == null) {
            return Paths.get(base + "_iterations.txt");
        }
        return parent.resolve(base + "_iterations.txt");
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }
}
