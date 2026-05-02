package util;

import model.Puzzle;
import solver.HeuristicType;
import solver.SearchAlgorithm;
import solver.SearchNode;
import solver.SearchResult;
import solver.SearchState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SolutionWriter {
    public void writeSolution(
            Path outputPath,
            Path inputPath,
            Puzzle puzzle,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType,
            SearchResult result
    ) throws IOException {
        ensureParentDirectory(outputPath);
        Files.writeString(outputPath, buildSolutionText(inputPath, puzzle, algorithm, heuristicType, result));
    }

    public void writeExploredSnapshots(
            Path outputPath,
            Path inputPath,
            Puzzle puzzle,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType,
            SearchResult result
    ) throws IOException {
        ensureParentDirectory(outputPath);
        Files.writeString(outputPath, buildExploredText(inputPath, puzzle, algorithm, heuristicType, result));
    }

    private String buildSolutionText(
            Path inputPath,
            Puzzle puzzle,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType,
            SearchResult result
    ) {
        StringBuilder builder = new StringBuilder();
        appendMetadata(builder, inputPath, algorithm, heuristicType, result);
        builder.append(System.lineSeparator());

        if (!result.isFound()) {
            builder.append("Solusi tidak ditemukan.").append(System.lineSeparator());
            return builder.toString();
        }

        List<SearchNode> path = result.getSolutionPath();
        for (int index = 0; index < path.size(); index++) {
            SearchNode node = path.get(index);
            if (index == 0) {
                builder.append("Initial").append(System.lineSeparator());
            } else {
                builder.append("Step ").append(index)
                        .append(" : ").append(node.getMoveFromParent().getCode())
                        .append(System.lineSeparator());
            }
            builder.append(BoardRenderer.renderState(puzzle, node.getState()))
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    private String buildExploredText(
            Path inputPath,
            Puzzle puzzle,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType,
            SearchResult result
    ) {
        StringBuilder builder = new StringBuilder();
        appendMetadata(builder, inputPath, algorithm, heuristicType, result);
        builder.append(System.lineSeparator());
        builder.append("Expanded iteration snapshots").append(System.lineSeparator());
        builder.append("============================").append(System.lineSeparator());

        List<SearchState> snapshots = result.getExploredSnapshots();
        for (int index = 0; index < snapshots.size(); index++) {
            SearchState state = snapshots.get(index);
            builder.append("Iteration ").append(index + 1)
                    .append(" | nextCheckpointIndex=")
                    .append(state.getNextCheckpointIndex())
                    .append(System.lineSeparator());
            builder.append(BoardRenderer.renderState(puzzle, state))
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    private void appendMetadata(
            StringBuilder builder,
            Path inputPath,
            SearchAlgorithm algorithm,
            HeuristicType heuristicType,
            SearchResult result
    ) {
        builder.append("Input file: ").append(inputPath).append(System.lineSeparator());
        builder.append("Algorithm: ").append(algorithm.getDisplayName()).append(System.lineSeparator());
        if (heuristicType != null) {
            builder.append("Heuristic: ").append(heuristicType).append(System.lineSeparator());
        }
        builder.append("Solusi Yang Ditemukan: ")
                .append(result.isFound() ? result.getSolutionMoves() : "-")
                .append(System.lineSeparator());
        builder.append("Cost dari Solusi: ")
                .append(result.isFound() ? result.getTotalCost() : "-")
                .append(System.lineSeparator());
        builder.append("Waktu eksekusi: ")
                .append(result.getExecutionTimeMillis())
                .append(" ms")
                .append(System.lineSeparator());
        builder.append("Banyak iterasi: ")
                .append(result.getIterationCount())
                .append(" iterasi")
                .append(System.lineSeparator());
    }

    private void ensureParentDirectory(Path outputPath) throws IOException {
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
