package solver;

import model.Position;

import java.util.Objects;

public final class SearchState {
    private final Position actorPosition;
    private final int nextCheckpointIndex;

    public SearchState(Position actorPosition, int nextCheckpointIndex) {
        this.actorPosition = actorPosition;
        this.nextCheckpointIndex = nextCheckpointIndex;
    }

    public Position getActorPosition() {
        return actorPosition;
    }

    public int getNextCheckpointIndex() {
        return nextCheckpointIndex;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SearchState)) {
            return false;
        }
        SearchState that = (SearchState) other;
        return nextCheckpointIndex == that.nextCheckpointIndex
                && Objects.equals(actorPosition, that.actorPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actorPosition, nextCheckpointIndex);
    }

    @Override
    public String toString() {
        return "SearchState{actorPosition=" + actorPosition
                + ", nextCheckpointIndex=" + nextCheckpointIndex + "}";
    }
}
