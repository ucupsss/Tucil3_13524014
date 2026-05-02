package solver;

import model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SlideResult {
    private final SearchState state;
    private final int moveCost;
    private final List<Position> positionsPassed;

    SlideResult(SearchState state, int moveCost, List<Position> positionsPassed) {
        this.state = state;
        this.moveCost = moveCost;
        this.positionsPassed = Collections.unmodifiableList(new ArrayList<Position>(positionsPassed));
    }

    SearchState getState() {
        return state;
    }

    int getMoveCost() {
        return moveCost;
    }

    List<Position> getPositionsPassed() {
        return positionsPassed;
    }
}
