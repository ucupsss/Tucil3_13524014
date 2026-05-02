package solver;

public enum SearchAlgorithm {
    UCS,
    GBFS,
    ASTAR;

    public static SearchAlgorithm fromInput(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Algorithm cannot be empty.");
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.equals("UCS")) {
            return UCS;
        }
        if (normalized.equals("GBFS")) {
            return GBFS;
        }
        if (normalized.equals("A*") || normalized.equals("ASTAR") || normalized.equals("A_STAR")) {
            return ASTAR;
        }
        throw new IllegalArgumentException("Unknown algorithm: " + value + ". Use UCS, GBFS, or A*.");
    }

    public String getDisplayName() {
        if (this == ASTAR) {
            return "A*";
        }
        return name();
    }
}
