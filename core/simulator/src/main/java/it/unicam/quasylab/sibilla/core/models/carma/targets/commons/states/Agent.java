package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;


import java.util.*;

public record Agent(int species, List<SibillaValue> values) {
    public final static Agent NIL = new Agent(-1,Collections.emptyList());
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        if (species != agent.species) return false;
        for (int i = 0; i < values.size(); ++i) {
            if (!values.get(i).equals(agent.values.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(species, values);
    }
}