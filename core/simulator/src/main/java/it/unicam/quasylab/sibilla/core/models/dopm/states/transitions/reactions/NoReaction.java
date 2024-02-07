package it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.Stream;

public class NoReaction implements Reaction{
    private final Agent agent;
    private final long total;

    public NoReaction(Agent agent, long total) {
        this.agent = agent;
        this.total = total;
    }

    public Agent getAgent() {
        return agent;
    }

    public long getTotal() {
        return total;
    }

    @Override
    public Stream<AgentDelta> sampleDeltas(Agent sender, DataOrientedPopulationState state, RandomGenerator rg) {
        return Stream.of(new AgentDelta(this.agent, this.total));
    }
}
