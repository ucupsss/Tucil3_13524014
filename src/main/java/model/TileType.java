package model;

public enum TileType {
    ICE('*'),
    OBSTACLE('X'),
    LAVA('L'),
    START('Z'),
    GOAL('O'),
    CHECKPOINT('?');

    private final char symbol;

    TileType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public static boolean isAllowedSymbol(char symbol) {
        return symbol == '*'
                || symbol == 'X'
                || symbol == 'L'
                || symbol == 'Z'
                || symbol == 'O'
                || isCheckpointSymbol(symbol);
    }

    public static boolean isCheckpointSymbol(char symbol) {
        return symbol >= '0' && symbol <= '9';
    }

    public static TileType fromSymbol(char symbol) {
        if (symbol == '*') {
            return ICE;
        }
        if (symbol == 'X') {
            return OBSTACLE;
        }
        if (symbol == 'L') {
            return LAVA;
        }
        if (symbol == 'Z') {
            return START;
        }
        if (symbol == 'O') {
            return GOAL;
        }
        if (isCheckpointSymbol(symbol)) {
            return CHECKPOINT;
        }
        throw new IllegalArgumentException("Unsupported tile symbol: " + symbol);
    }
}
