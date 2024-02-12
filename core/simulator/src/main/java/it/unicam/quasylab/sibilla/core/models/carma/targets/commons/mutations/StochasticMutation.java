package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.stream.IntStream;

public class StochasticMutation implements Mutation {
    private final List<StochasticMutationTuple> mutationTuples;

    public StochasticMutation(List<StochasticMutationTuple> mutationTuples) {
        this.mutationTuples = mutationTuples;
    }

    @Override
    public MutationResult sampleDeltas(ExpressionContext context, long numberOf, RandomGenerator rg) {

        long[] mutating = new long[mutationTuples.size()];
        long nonMutated = 0;

        WeightedStructure<Integer> probabilities = new WeightedLinkedList<>();

        for(int i=0; i<mutationTuples.size(); ++i) {
            probabilities.add(mutationTuples.get(i).probability().eval(context).doubleOf(), i);
        }

        for(int i=0; i<numberOf; ++i) {
            double sample = rg.nextDouble() * probabilities.getTotalWeight();
            WeightedElement<Integer> indexElement = probabilities.select(sample);
            if(indexElement != null) {
                mutating[indexElement.getElement()]++;
            } else {
                nonMutated++;
            }
        }

        return new MutationResult(
            IntStream
                .range(0, mutationTuples.size())
                .filter(i -> mutating[i] > 0)
                .mapToObj(i -> new AgentDelta(
                        mutationTuples.get(i).agentExpression().eval(context),
                        mutating[i]
                )),
                nonMutated
        );
    }
}
