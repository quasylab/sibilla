package it.unicam.quasylab.sibilla.langs.enba.symbols;

/**
 * Describes the info associated to a given symbol.
 */
public enum Type {
    REAL,
    INTEGER,
    BOOLEAN,
    ERROR;

    @Override
    public String toString() {
        switch (this) {
            case REAL: return "real";
            case INTEGER: return "integer";
            case BOOLEAN: return "boolean";
            default:
                return super.toString();
        }
    }

    public boolean assignmentCompatible(Type assigment) {
        return (assigment == this) || (this == REAL && assigment == INTEGER);
    }

    public static Type fromString(String type) {
        return switch (type.toLowerCase()) {
            case "real" -> REAL;
            case "integer" -> INTEGER;
            case "boolean" -> BOOLEAN;
            default -> ERROR;
        };
    }
}
