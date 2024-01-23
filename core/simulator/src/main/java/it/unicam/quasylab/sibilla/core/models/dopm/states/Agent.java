package it.unicam.quasylab.sibilla.core.models.dopm.states;

import it.unicam.quasylab.sibilla.core.models.dopm.functions.ReferenceSolverFunction;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        if (!species.equals(agent.species)) return false;
        for(String k : values.keySet()) {
            if(!agent.getValues().containsKey(k) || agent.getValues().get(k).doubleOf() != values.get(k).doubleOf()) {
                return false;
            }
        }
        return true;
    }

    public ReferenceSolverFunction getResolver() {
        return name -> Optional.ofNullable(this.values.get(name));
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = species.hashCode();
        for(String k : values.keySet()) {
            result = 31 * result + k.hashCode();
            temp = Double.doubleToLongBits(values.get(k).doubleOf());
            result = 31 * result + (int) (temp ^ (temp >>> 32));
        }
        return result;
    }

    @Override
    public String toString() {
        String r = species + "[";
        for(Map.Entry<String, SibillaValue> e : values.entrySet()) {
            r+= e.getKey() + "=";
            r+=e.getValue()+",";
        }
        r += "]";
        return r;
    }
}