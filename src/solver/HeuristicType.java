package solver;

public enum HeuristicType {
    H0,
    H1,
    H2,
    H3;

    public static HeuristicType fromInput(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Heuristic cannot be empty.");
        }
        String normalized = value.trim().toUpperCase();
        for (HeuristicType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown heuristic: " + value + ". Use H0, H1, H2, or H3.");
    }
}
