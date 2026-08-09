package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.Mutation;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public record InputTransition(BiPredicate<Integer, ExpressionContext> predicate,
                              Predicate<ExpressionContext> senderPredicate,
                              Function<ExpressionContext, Double> probability, Mutation post) {
}
