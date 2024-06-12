package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.AgentExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.Stream;

public class DeterministicMutation implements Mutation {

    private final AgentExpressionFunction agentExpressionFunction;

    public DeterministicMutation(AgentExpressionFunction agentExpressionFunction) {
        this.agentExpressionFunction = agentExpressionFunction;
    }

    @Override
    public MutationResult sampleDeltas(ExpressionContext context, long numberOf, RandomGenerator rg) {
        return new MutationResult(Stream.of(new AgentDelta(agentExpressionFunction.eval(context), numberOf)), 0);
    }
}
