package it.unicam.quasylab.sibilla.langs.dopm.symbols;

/**
 * Describes the info associated to a given symbol.
 */
public enum SymbolType {
    NUMBER,
    BOOLEAN,
    ERROR;

    public boolean isANumber() {
        return (this==ERROR)||(this==NUMBER);
    }

    public boolean isABoolean() {
        return (this==ERROR)||(this==BOOLEAN);
    }

    @Override
    public String toString() {
        switch (this) {
            case NUMBER: return "number";
            case BOOLEAN: return "boolean";
            default:
                return super.toString();
        }
    }

    public String javaType() {
        switch (this) {
            case NUMBER: return "double";
            case BOOLEAN: return "boolean";
            default: return "Object";
        }
    }
}
