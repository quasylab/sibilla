package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.states.transitions.reactions;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.MutationResult;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.Stream;

public class InputReaction implements Reaction {
    private final Agent agent;
    private final long total;
    private final InputTransition input;

    public InputReaction(Agent agent, long total, InputTransition input) {
        this.agent = agent;
        this.total = total;
        this.input = input;
    }

    public Agent getAgent() {
        return agent;
    }

    public long getTotal() {
        return total;
    }

    public InputTransition getInput() {
        return input;
    }

    @Override
    public Stream<AgentDelta> sampleDeltas(Agent sender, AgentState state, RandomGenerator rg) {
        double probability = input.probability().apply(new ExpressionContext(agent.values(),null, state));
        long transitioning = Stream.generate(rg::nextDouble)
                .limit(this.total)
                .filter(result -> result <= probability)
                .count();
        MutationResult result = input.post()
                .sampleDeltas(new ExpressionContext(agent.values(), sender.values(), state), transitioning, rg);
        transitioning -= result.nonMutated();
        return transitioning < this.total
                ? Stream.concat(Stream.of(new AgentDelta(agent, total-transitioning)), result.agentDeltaStream())
                : result.agentDeltaStream();
    }
}
