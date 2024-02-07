package it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.Stream;

public interface Reaction {
    Stream<AgentDelta> sampleDeltas(Agent sender, DataOrientedPopulationState state, RandomGenerator rg);
}
