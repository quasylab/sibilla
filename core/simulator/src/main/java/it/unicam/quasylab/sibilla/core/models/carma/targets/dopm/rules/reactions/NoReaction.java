package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.reactions;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.Stream;

public record NoReaction(Agent agent, long total) implements Reaction {

    @Override
    public Stream<AgentDelta> sampleDeltas(Agent sender, AgentState state, RandomGenerator rg) {
        return Stream.of(new AgentDelta(this.agent, this.total));
    }
}
