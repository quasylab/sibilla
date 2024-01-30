package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.mutations.Mutation;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public record InputTransition(BiPredicate<Integer, ExpressionContext> predicate,
                              Predicate<ExpressionContext> senderPredicate,
                              Function<ExpressionContext, Double> probability, Mutation post) {
}
