package solver;

import model.Board;
import model.Direction;
import model.Position;
import model.Puzzle;

import java.util.ArrayList;
import java.util.List;

final class SlideSimulator {
    SlideResult slide(Puzzle puzzle, SearchState state, Direction direction) {
        Board board = puzzle.getBoard();
        Position current = state.getActorPosition();
        int nextCheckpointIndex = state.getNextCheckpointIndex();
        int cost = 0;
        List<Position> positionsPassed = new ArrayList<Position>();

        while (true) {
            Position next = current.move(direction);
            if (!board.inBounds(next)) {
                return null;
            }
            if (board.isObstacle(next)) {
                if (positionsPassed.isEmpty()) {
                    return null;
                }
                return new SlideResult(
                        new SearchState(current, nextCheckpointIndex),
                        cost,
                        positionsPassed);
            }
            if (board.isLava(next)) {
                return null;
            }

            cost += board.getCost(next);
            positionsPassed.add(next);

            if (board.isCheckpoint(next)) {
                int checkpointIndex = board.getTile(next) - '0';
                if (checkpointIndex < nextCheckpointIndex) {
                    // Already passed checkpoints behave like normal traversable ice.
                } else if (checkpointIndex == nextCheckpointIndex) {
                    nextCheckpointIndex++;
                } else {
                    return null;
                }
            }

            current = next;
        }
    }
}
