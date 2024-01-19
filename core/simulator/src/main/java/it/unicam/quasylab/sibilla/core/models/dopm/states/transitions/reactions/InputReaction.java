package it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions;

import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.IntStream;
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

    @Override
    public Stream<AgentDelta> sampleDeltas(Agent sender, DataOrientedPopulationState state, RandomGenerator rg) {
        long transitioning = Stream.generate(rg::nextDouble)
                .limit(this.total)
                .filter(result -> result <= input.getProbability().apply(state, this.agent))
                .count();
        Stream<AgentDelta> transitionedStream = Stream
                .generate(() -> new AgentDelta(input.getPost().apply(sender, this.agent), 1))
                .limit(transitioning);
        return transitioning < this.total
                ? Stream.concat(Stream.of(new AgentDelta(agent, total - transitioning)),transitionedStream)
                : transitionedStream;
    }
}
