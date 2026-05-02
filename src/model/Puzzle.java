package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Puzzle {
    private final Board board;
    private final Position startPosition;
    private final Position goalPosition;
    private final List<Position> checkpointPositions;

    public Puzzle(Board board, Position startPosition, Position goalPosition, List<Position> checkpointPositions) {
        this.board = board;
        this.startPosition = startPosition;
        this.goalPosition = goalPosition;
        this.checkpointPositions = Collections.unmodifiableList(new ArrayList<Position>(checkpointPositions));
    }

    public Board getBoard() {
        return board;
    }

    public int getRowCount() {
        return board.getRowCount();
    }

    public int getColCount() {
        return board.getColCount();
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getGoalPosition() {
        return goalPosition;
    }

    public List<Position> getCheckpointPositions() {
        return checkpointPositions;
    }

    public String renderInitialBoard() {
        return board.render(startPosition);
    }
}
