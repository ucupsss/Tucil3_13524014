package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SearchResult {
    private final boolean found;
    private final String solutionMoves;
    private final int totalCost;
    private final long iterationCount;
    private final long executionTimeMillis;
    private final SearchNode finalNode;
    private final List<SearchNode> solutionPath;
    private final List<SearchState> solutionStates;
    private final List<SearchState> exploredSnapshots;

    public SearchResult(
            boolean found,
            String solutionMoves,
            int totalCost,
            long iterationCount,
            long executionTimeMillis,
            SearchNode finalNode,
            List<SearchNode> solutionPath,
            List<SearchState> solutionStates,
            List<SearchState> exploredSnapshots
    ) {
        this.found = found;
        this.solutionMoves = solutionMoves;
        this.totalCost = totalCost;
        this.iterationCount = iterationCount;
        this.executionTimeMillis = executionTimeMillis;
        this.finalNode = finalNode;
        this.solutionPath = immutableCopy(solutionPath);
        this.solutionStates = immutableCopy(solutionStates);
        this.exploredSnapshots = immutableCopy(exploredSnapshots);
    }

    public boolean isFound() {
        return found;
    }

    public String getSolutionMoves() {
        return solutionMoves;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public long getIterationCount() {
        return iterationCount;
    }

    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    public SearchNode getFinalNode() {
        return finalNode;
    }

    public List<SearchNode> getSolutionPath() {
        return solutionPath;
    }

    public List<SearchState> getSolutionStates() {
        return solutionStates;
    }

    public List<SearchState> getExploredSnapshots() {
        return exploredSnapshots;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
