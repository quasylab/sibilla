package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;


import java.util.*;

public record Agent(int species, Map<String,SibillaValue> values) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        if (species != agent.species) return false;
        return values.entrySet()
                .stream()
                .allMatch(e -> e.getValue().equals(agent.values.get(e.getKey())));
    }

    @Override
    public int hashCode() {
        return Objects.hash(species, values);
    }
}