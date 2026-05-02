package util;

import model.Position;
import model.Puzzle;
import solver.SearchState;

public final class BoardRenderer {
    private BoardRenderer() {
    }

    public static String renderState(Puzzle puzzle, SearchState state) {
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
