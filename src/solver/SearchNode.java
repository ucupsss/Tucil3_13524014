package solver;

import model.Direction;
import model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SearchNode {
    private final SearchState state;
    private final SearchNode parent;
    private final Direction moveFromParent;
    private final int g;
    private final int h;
    private final int f;
    private final List<Position> positionsPassedFromParent;

    public SearchNode(
            SearchState state,
            SearchNode parent,
            Direction moveFromParent,
            int g,
            int h,
            int f,
            List<Position> positionsPassedFromParent
    ) {
        this.state = state;
        this.parent = parent;
        this.moveFromParent = moveFromParent;
        this.g = g;
        this.h = h;
        this.f = f;
        this.positionsPassedFromParent = Collections.unmodifiableList(
                new ArrayList<Position>(positionsPassedFromParent));
    }

    public SearchState getState() {
        return state;
    }

    public SearchNode getParent() {
        return parent;
    }

    public Direction getMoveFromParent() {
        return moveFromParent;
    }

    public int getG() {
        return g;
    }

    public int getH() {
        return h;
    }

    public int getF() {
        return f;
    }

    public List<Position> getPositionsPassedFromParent() {
        return positionsPassedFromParent;
    }
}
