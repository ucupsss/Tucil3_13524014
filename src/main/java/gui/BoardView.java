package gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Board;
import model.Position;
import model.Puzzle;
import solver.SearchNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BoardView extends Pane {
    private static final String DISPLAY_FONT = "Bahnschrift";
    private static final String MONO_FONT = "Cascadia Code";
    private static final Color BACKGROUND = Color.web("#f0f4f8");
    private static final Color ICE = Color.web("#dff3ff");
    private static final Color WALL = Color.web("#dfe3e7");
    private static final Color LAVA = Color.web("#ffdad3");
    private static final Color GOAL = Color.web("#b4ebff");
    private static final Color CHECKPOINT = Color.web("#ffe6a3");
    private static final Color PASSED = Color.web("#3cd7ff", 0.35);
    private static final Color GRID = Color.web("#f6fafe");
    private static final Color ACTOR = Color.web("#0058be");
    private static final Color TEXT = Color.web("#171c1f");

    private final Canvas canvas = new Canvas();
    private Puzzle puzzle;
    private SearchNode currentNode;

    public BoardView() {
        getChildren().add(canvas);
        setStyle("-fx-background-color: #f0f4f8;");
        widthProperty().addListener((observable, oldValue, newValue) -> draw());
        heightProperty().addListener((observable, oldValue, newValue) -> draw());
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        draw();
    }

    public void showPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.currentNode = null;
        draw();
    }

    public void showStep(Puzzle puzzle, SearchNode node) {
        this.puzzle = puzzle;
        this.currentNode = node;
        draw();
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, width, height);

        if (puzzle == null || width <= 0 || height <= 0) {
            drawEmptyMessage(gc, width, height);
            return;
        }

        Board board = puzzle.getBoard();
        double padding = 28;
        double availableWidth = Math.max(0, width - 2 * padding);
        double availableHeight = Math.max(0, height - 2 * padding);
        double tileSize = Math.min(availableWidth / board.getColCount(), availableHeight / board.getRowCount());
        double boardWidth = tileSize * board.getColCount();
        double boardHeight = tileSize * board.getRowCount();
        double startX = (width - boardWidth) / 2;
        double startY = (height - boardHeight) / 2;
        Set<Position> passedPositions = new HashSet<Position>(currentPassedPositions());
        Position actor = currentActorPosition();
        int completedCheckpointCount = currentCompletedCheckpointCount();

        gc.setFill(Color.web("#ffffff"));
        gc.fillRoundRect(startX - 10, startY - 10, boardWidth + 20, boardHeight + 20, 8, 8);
        gc.setStroke(Color.web("#c1c6d7"));
        gc.strokeRoundRect(startX - 10, startY - 10, boardWidth + 20, boardHeight + 20, 8, 8);

        for (int row = 0; row < board.getRowCount(); row++) {
            for (int col = 0; col < board.getColCount(); col++) {
                Position position = new Position(row, col);
                double x = startX + col * tileSize;
                double y = startY + row * tileSize;
                char tile = board.getTile(position);
                drawTile(gc, position, tile, x, y, tileSize, passedPositions, actor, completedCheckpointCount);
            }
        }
    }

    private void drawTile(
            GraphicsContext gc,
            Position position,
            char tile,
            double x,
            double y,
            double size,
            Set<Position> passedPositions,
            Position actor,
            int completedCheckpointCount
    ) {
        double gap = Math.max(2, size * 0.04);
        double tileX = x + gap / 2;
        double tileY = y + gap / 2;
        double tileSize = Math.max(1, size - gap);

        gc.setFill(tileColor(tile, position, completedCheckpointCount));
        gc.fillRoundRect(tileX, tileY, tileSize, tileSize, 4, 4);
        if (passedPositions.contains(position)) {
            gc.setFill(PASSED);
            gc.fillRoundRect(tileX, tileY, tileSize, tileSize, 4, 4);
        }
        gc.setStroke(GRID);
        gc.strokeRoundRect(tileX, tileY, tileSize, tileSize, 4, 4);

        if (tile == 'X') {
            drawCenteredText(gc, "#", x, y, size, Color.web("#5d6a7d"), FontWeight.BOLD, 0.36);
        } else if (tile == 'L') {
            drawCenteredText(gc, "L", x, y, size, Color.web("#b51c00"), FontWeight.BOLD, 0.38);
        } else if (tile == 'O') {
            drawCenteredText(gc, "O", x, y, size, Color.web("#00677e"), FontWeight.BOLD, 0.38);
        } else if (tile >= '0' && tile <= '9' && !isCompletedCheckpoint(position, completedCheckpointCount)) {
            drawCenteredText(gc, String.valueOf(tile), x, y, size, Color.web("#8e1300"), FontWeight.BOLD, 0.38);
        }

        if (size >= 34 && tile != 'X' && tile != 'L') {
            gc.setFill(Color.web("#414755", 0.55));
            gc.setFont(Font.font(MONO_FONT, FontWeight.NORMAL, Math.max(8, size * 0.16)));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(String.valueOf(puzzle.getBoard().getCost(position)), x + size - gap - 3, y + size - gap - 4);
        }

        if (position.equals(actor)) {
            gc.setFill(ACTOR);
            gc.fillOval(x + size * 0.24, y + size * 0.24, size * 0.52, size * 0.52);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(Math.max(2, size * 0.05));
            gc.strokeOval(x + size * 0.24, y + size * 0.24, size * 0.52, size * 0.52);
        }
    }

    private Color tileColor(char tile, Position position, int completedCheckpointCount) {
        if (tile == 'X') {
            return WALL;
        }
        if (tile == 'L') {
            return LAVA;
        }
        if (tile == 'O') {
            return GOAL;
        }
        if (tile >= '0' && tile <= '9' && !isCompletedCheckpoint(position, completedCheckpointCount)) {
            return CHECKPOINT;
        }
        return ICE;
    }

    private void drawCenteredText(
            GraphicsContext gc,
            String text,
            double x,
            double y,
            double size,
            Color color,
            FontWeight weight,
            double scale
    ) {
        gc.setFill(color);
        gc.setFont(Font.font(DISPLAY_FONT, weight, Math.max(10, size * scale)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x + size / 2, y + size * 0.62);
    }

    private void drawEmptyMessage(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.web("#414755"));
        gc.setFont(Font.font(DISPLAY_FONT, FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Choose a puzzle file, then click Solve.", width / 2, height / 2);
    }

    private Position currentActorPosition() {
        if (currentNode != null) {
            return currentNode.getState().getActorPosition();
        }
        return puzzle.getStartPosition();
    }

    private int currentCompletedCheckpointCount() {
        if (currentNode != null) {
            return currentNode.getState().getNextCheckpointIndex();
        }
        return 0;
    }

    private List<Position> currentPassedPositions() {
        if (currentNode == null) {
            return Collections.emptyList();
        }
        return currentNode.getPositionsPassedFromParent();
    }

    private boolean isCompletedCheckpoint(Position position, int completedCheckpointCount) {
        for (int index = 0; index < completedCheckpointCount; index++) {
            if (puzzle.getCheckpointPositions().get(index).equals(position)) {
                return true;
            }
        }
        return false;
    }
}
