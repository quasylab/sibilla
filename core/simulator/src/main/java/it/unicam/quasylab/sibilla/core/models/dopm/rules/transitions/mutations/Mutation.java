package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.mutations;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.AgentDelta;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.Stream;

public interface Mutation {
    Stream<AgentDelta> sampleDeltas(ExpressionContext context, long numberOf, RandomGenerator rg) ;
}
