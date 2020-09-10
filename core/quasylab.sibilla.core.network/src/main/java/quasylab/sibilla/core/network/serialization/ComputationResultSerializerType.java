package quasylab.sibilla.core.network.serialization;

public enum ComputationResultSerializerType {
    APACHE("a", "Apache"), FST("f", "Fst"), CUSTOM("c", "Custom");
    private final String label;
    private final String fullName;

    public String getLabel() {
        return label;
    }

    public String getFullName() {
        return fullName;
    }

    ComputationResultSerializerType(String label, String fullName) {
        this.fullName = fullName;
        this.label = label;
    }
}
