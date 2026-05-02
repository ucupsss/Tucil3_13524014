package solver;

import model.Direction;
import model.Puzzle;
import util.PathReconstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class GreedyBestFirstSolver {
    private final SlideSimulator slideSimulator = new SlideSimulator();
    private final Heuristic heuristic;

    public GreedyBestFirstSolver(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    public SearchResult solve(Puzzle puzzle) {
        long startTime = System.nanoTime();

        Map<SearchState, Integer> bestG = new HashMap<SearchState, Integer>();
        List<SearchState> exploredSnapshots = new ArrayList<SearchState>();
        PriorityQueue<QueueEntry> frontier = new PriorityQueue<QueueEntry>(
                Comparator.comparingInt((QueueEntry entry) -> entry.node.getF())
                        .thenComparingInt(entry -> entry.node.getG())
                        .thenComparingLong(entry -> entry.insertionOrder));

        long insertionOrder = 0;
        long iterations = 0;
        SearchState startState = new SearchState(puzzle.getStartPosition(), 0);
        int startH = heuristic.estimate(puzzle, startState);
        SearchNode startNode = new SearchNode(
                startState,
                null,
                null,
                0,
                startH,
                startH,
                new ArrayList<>());

        bestG.put(startState, 0);
        frontier.add(new QueueEntry(startNode, insertionOrder++));

        while (!frontier.isEmpty()) {
            SearchNode currentNode = frontier.poll().node;
            Integer bestKnownCost = bestG.get(currentNode.getState());
            if (bestKnownCost == null || currentNode.getG() != bestKnownCost) {
                continue;
            }

            iterations++;
            exploredSnapshots.add(currentNode.getState());

            if (isGoal(puzzle, currentNode.getState())) {
                long executionTimeMillis = elapsedMillis(startTime);
                List<SearchNode> path = PathReconstructor.reconstructNodes(currentNode);
                return new SearchResult(
                        true,
                        PathReconstructor.reconstructMoves(currentNode),
                        currentNode.getG(),
                        iterations,
                        executionTimeMillis,
                        currentNode,
                        path,
                        PathReconstructor.reconstructStates(currentNode),
                        exploredSnapshots);
            }

            for (Direction direction : Direction.values()) {
                SlideResult slideResult = slideSimulator.slide(puzzle, currentNode.getState(), direction);
                if (slideResult == null) {
                    continue;
                }

                int newG = currentNode.getG() + slideResult.getMoveCost();
                SearchState nextState = slideResult.getState();
                Integer previousBest = bestG.get(nextState);
                if (previousBest != null && newG >= previousBest) {
                    continue;
                }

                int h = heuristic.estimate(puzzle, nextState);
                SearchNode nextNode = new SearchNode(
                        nextState,
                        currentNode,
                        direction,
                        newG,
                        h,
                        h,
                        slideResult.getPositionsPassed());

                bestG.put(nextState, newG);
                frontier.add(new QueueEntry(nextNode, insertionOrder++));
            }
        }

        long executionTimeMillis = elapsedMillis(startTime);
        return new SearchResult(
                false,
                "",
                -1,
                iterations,
                executionTimeMillis,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                exploredSnapshots);
    }

    private boolean isGoal(Puzzle puzzle, SearchState state) {
        return state.getActorPosition().equals(puzzle.getGoalPosition())
                && state.getNextCheckpointIndex() == puzzle.getCheckpointPositions().size();
    }

    private long elapsedMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000L;
    }

    private static final class QueueEntry {
        private final SearchNode node;
        private final long insertionOrder;

        private QueueEntry(SearchNode node, long insertionOrder) {
            this.node = node;
            this.insertionOrder = insertionOrder;
        }
    }
}
