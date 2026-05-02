package solver;

public enum SearchAlgorithm {
    UCS,
    GBFS;

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
        throw new IllegalArgumentException("Unknown algorithm: " + value + ". Use UCS or GBFS.");
    }
}
