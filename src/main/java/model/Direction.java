package model;

public enum Direction {
    UP('U', -1, 0),
    DOWN('D', 1, 0),
    LEFT('L', 0, -1),
    RIGHT('R', 0, 1);

    private final char code;
    private final int deltaRow;
    private final int deltaCol;

    Direction(char code, int deltaRow, int deltaCol) {
        this.code = code;
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    public char getCode() {
        return code;
    }

    public int getDeltaRow() {
        return deltaRow;
    }

    public int getDeltaCol() {
        return deltaCol;
    }
}
