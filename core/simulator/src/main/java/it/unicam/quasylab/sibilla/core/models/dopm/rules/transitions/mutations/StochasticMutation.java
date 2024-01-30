package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.mutations;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.AgentDelta;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StochasticMutation implements Mutation {
    private final List<StochasticMutationTuple> mutationTuples;

    public StochasticMutation(List<StochasticMutationTuple> mutationTuples) {
        this.mutationTuples = mutationTuples;
    }

    @Override
    public Stream<AgentDelta> sampleDeltas(ExpressionContext context, long numberOf, RandomGenerator rg) {

        long[] mutating = new long[mutationTuples.size()];

        WeightedStructure<Integer> probabilities = new WeightedLinkedList<>();
        for(int i=0; i<mutationTuples.size(); ++i) {
            probabilities.add(mutationTuples.get(i).probability().eval(context).doubleOf(), i);
        }

        for(int i=0; i<numberOf; ++i) {
            double sample = rg.nextDouble() * probabilities.getTotalWeight();
            mutating[probabilities.select(sample).getElement()]++;
        }

        return IntStream
                    .range(0, mutationTuples.size())
                    .filter(i -> mutating[i] > 0)
                    .mapToObj(i -> new AgentDelta(
                            mutationTuples.get(i).agentExpression().eval(context),
                            mutating[i]
                    ));
    }
}
