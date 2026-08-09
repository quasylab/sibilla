package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import org.apache.commons.math3.random.RandomGenerator;

public interface Mutation {
    MutationResult sampleDeltas(ExpressionContext context, long numberOf, RandomGenerator rg) ;
}
