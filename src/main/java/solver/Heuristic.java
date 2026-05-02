package solver;

import model.Puzzle;

public interface Heuristic {
    int estimate(Puzzle puzzle, SearchState state);

    HeuristicType getType();
}
