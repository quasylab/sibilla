package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.mutations.Mutation;

import java.util.function.BiPredicate;
import java.util.function.Function;

public record OutputTransition(BiPredicate<Integer, ExpressionContext> predicate,
                               Function<ExpressionContext, Double> rate, Mutation post) {
}
