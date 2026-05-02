package model;

public final class Board {
    private final int rowCount;
    private final int colCount;
    private final char[][] tiles;
    private final int[][] traversalCosts;

    public Board(int rowCount, int colCount, char[][] tiles, int[][] traversalCosts) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.tiles = copyTiles(tiles);
        this.traversalCosts = copyCosts(traversalCosts);
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public boolean inBounds(Position position) {
        return position != null
                && position.getRow() >= 0
                && position.getRow() < rowCount
                && position.getCol() >= 0
                && position.getCol() < colCount;
    }

    public char getTile(Position position) {
        requireInBounds(position);
        return tiles[position.getRow()][position.getCol()];
    }

    public int getCost(Position position) {
        requireInBounds(position);
        return traversalCosts[position.getRow()][position.getCol()];
    }

    public boolean isObstacle(Position position) {
        return getTile(position) == 'X';
    }

    public boolean isLava(Position position) {
        return getTile(position) == 'L';
    }

    public boolean isGoal(Position position) {
        return getTile(position) == 'O';
    }

    public boolean isCheckpoint(Position position) {
        return TileType.isCheckpointSymbol(getTile(position));
    }

    public int[][] copyCostMatrix() {
        return copyCosts(traversalCosts);
    }

    public String render(Position actorPosition) {
        if (actorPosition != null) {
            requireInBounds(actorPosition);
        }

        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < rowCount; row++) {
            if (row > 0) {
                builder.append(System.lineSeparator());
            }
            for (int col = 0; col < colCount; col++) {
                Position current = new Position(row, col);
                if (current.equals(actorPosition)) {
                    builder.append('Z');
                } else if (tiles[row][col] == 'Z') {
                    builder.append('*');
                } else {
                    builder.append(tiles[row][col]);
                }
            }
        }
        return builder.toString();
    }

    private void requireInBounds(Position position) {
        if (!inBounds(position)) {
            throw new IllegalArgumentException("Position out of bounds: " + position);
        }
    }

    private static char[][] copyTiles(char[][] source) {
        char[][] copy = new char[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = new char[source[row].length];
            System.arraycopy(source[row], 0, copy[row], 0, source[row].length);
        }
        return copy;
    }

    private static int[][] copyCosts(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = new int[source[row].length];
            System.arraycopy(source[row], 0, copy[row], 0, source[row].length);
        }
        return copy;
    }
}
