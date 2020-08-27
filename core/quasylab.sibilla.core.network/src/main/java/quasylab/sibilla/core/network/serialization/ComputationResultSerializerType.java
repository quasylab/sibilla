package quasylab.sibilla.core.network.serialization;

public enum ComputationResultSerializerType {
    APACHE("a", "Apache"), FST("f", "Fst"), CUSTOM("c", "Custom");
    private String label;
    private String fullName;

    public String getLabel() {
        return label;
    }

    public String getFullName() {
        return fullName;
    }

    private ComputationResultSerializerType(String label, String fullName) {
        this.fullName = fullName;
        this.label = label;
    }
}
