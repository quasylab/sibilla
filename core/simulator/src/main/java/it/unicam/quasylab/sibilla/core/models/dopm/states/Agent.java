package it.unicam.quasylab.sibilla.core.models.dopm.states;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;

public class Agent {
    private String species;
    private Map<String, SibillaValue> values;

    public Agent(String species, Map<String, SibillaValue> values) {
        this.species = species;
        this.values = values;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public Map<String, SibillaValue> getValues() {
        return values;
    }

    public void setValues(Map<String, SibillaValue> values) {
        this.values = values;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Agent(new String(species), new HashMap<>(values));
    }
}
