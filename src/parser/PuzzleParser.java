package parser;

import model.Board;
import model.Position;
import model.Puzzle;
import model.TileType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PuzzleParser {
    public Puzzle parse(Path path) throws InvalidPuzzleException {
        if (path == null) {
            throw new InvalidPuzzleException("Input path cannot be null.");
        }
        if (!Files.exists(path)) {
            throw new InvalidPuzzleException("Input file does not exist: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new InvalidPuzzleException("Input path is not a file: " + path);
        }
        if (!path.getFileName().toString().toLowerCase().endsWith(".txt")) {
            throw new InvalidPuzzleException("Input file must use .txt extension: " + path);
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException exception) {
            throw new InvalidPuzzleException("Failed to read input file: " + exception.getMessage(), exception);
        }

        return parseLines(trimTrailingBlankLines(lines));
    }

    Puzzle parseLines(List<String> lines) throws InvalidPuzzleException {
        if (lines.isEmpty()) {
            throw new InvalidPuzzleException("Input file is empty.");
        }

        String[] dimensions = lines.get(0).trim().split("\\s+");
        if (dimensions.length != 2) {
            throw new InvalidPuzzleException("Line 1 must contain exactly two integers: N M.");
        }

        int rowCount = parsePositiveInteger(dimensions[0], "N", 1);
        int colCount = parsePositiveInteger(dimensions[1], "M", 1);
        int expectedLineCount = 1 + rowCount + rowCount;
        if (lines.size() != expectedLineCount) {
            throw new InvalidPuzzleException("Input has " + lines.size() + " non-trailing lines, but expected exactly "
                    + expectedLineCount + " lines for a " + rowCount + "x" + colCount + " board.");
        }

        char[][] tiles = new char[rowCount][colCount];
        int[][] costs = new int[rowCount][colCount];
        Position start = null;
        Position goal = null;
        Position[] checkpointByIndex = new Position[10];

        for (int row = 0; row < rowCount; row++) {
            String layoutLine = lines.get(1 + row).trim();
            if (layoutLine.length() != colCount) {
                throw new InvalidPuzzleException("Board row " + (row + 1) + " has length "
                        + layoutLine.length() + ", expected " + colCount + ".");
            }

            for (int col = 0; col < colCount; col++) {
                char symbol = layoutLine.charAt(col);
                validateTileSymbol(symbol, row, col);
                tiles[row][col] = symbol;

                if (symbol == 'Z') {
                    if (start != null) {
                        throw new InvalidPuzzleException("Board must contain exactly one start tile Z.");
                    }
                    start = new Position(row, col);
                } else if (symbol == 'O') {
                    if (goal != null) {
                        throw new InvalidPuzzleException("Board must contain exactly one goal tile O.");
                    }
                    goal = new Position(row, col);
                } else if (TileType.isCheckpointSymbol(symbol)) {
                    int checkpointIndex = symbol - '0';
                    if (checkpointByIndex[checkpointIndex] != null) {
                        throw new InvalidPuzzleException("Duplicate checkpoint tile '" + symbol + "' found.");
                    }
                    checkpointByIndex[checkpointIndex] = new Position(row, col);
                }
            }
        }

        for (int row = 0; row < rowCount; row++) {
            int lineNumber = 1 + rowCount + row + 1;
            String costLine = lines.get(1 + rowCount + row).trim();
            String[] values = costLine.split("\\s+");
            if (values.length != colCount) {
                throw new InvalidPuzzleException("Cost row " + (row + 1) + " on line " + lineNumber
                        + " has " + values.length + " values, expected " + colCount + ".");
            }
            for (int col = 0; col < colCount; col++) {
                costs[row][col] = parsePositiveInteger(values[col], "cost", lineNumber);
            }
        }

        if (start == null) {
            throw new InvalidPuzzleException("Board must contain exactly one start tile Z.");
        }
        if (goal == null) {
            throw new InvalidPuzzleException("Board must contain exactly one goal tile O.");
        }

        List<Position> checkpoints = collectAndValidateCheckpoints(checkpointByIndex);
        return new Puzzle(new Board(rowCount, colCount, tiles, costs), start, goal, checkpoints);
    }

    private void validateTileSymbol(char symbol, int row, int col) throws InvalidPuzzleException {
        if (!TileType.isAllowedSymbol(symbol)) {
            throw new InvalidPuzzleException("Invalid tile symbol '" + symbol + "' at row "
                    + (row + 1) + ", column " + (col + 1) + ".");
        }
    }

    private int parsePositiveInteger(String value, String name, int lineNumber) throws InvalidPuzzleException {
        int parsed = parseInteger(value, name, lineNumber);
        if (parsed <= 0) {
            throw new InvalidPuzzleException(name + " on line " + lineNumber + " must be a positive integer.");
        }
        return parsed;
    }

    private int parseInteger(String value, String name, int lineNumber) throws InvalidPuzzleException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidPuzzleException(name + " on line " + lineNumber
                    + " must be an integer, got '" + value + "'.");
        }
    }

    private List<Position> collectAndValidateCheckpoints(Position[] checkpointByIndex)
            throws InvalidPuzzleException {
        List<Position> checkpoints = new ArrayList<Position>();
        boolean gapFound = false;
        for (int index = 0; index < checkpointByIndex.length; index++) {
            Position checkpoint = checkpointByIndex[index];
            if (checkpoint == null) {
                gapFound = true;
            } else {
                if (gapFound) {
                    throw new InvalidPuzzleException("Checkpoint numbers must be contiguous from 0. Found checkpoint "
                            + index + " after a missing earlier checkpoint.");
                }
                checkpoints.add(checkpoint);
            }
        }
        return checkpoints;
    }

    private List<String> trimTrailingBlankLines(List<String> lines) {
        List<String> trimmed = new ArrayList<String>(lines);
        while (!trimmed.isEmpty() && trimmed.get(trimmed.size() - 1).trim().isEmpty()) {
            trimmed.remove(trimmed.size() - 1);
        }
        return trimmed;
    }
}
