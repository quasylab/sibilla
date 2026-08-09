package it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.Mutation;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public record InputAction(String channel,
                          BiPredicate<Integer, ExpressionContext> predicate,
                          Predicate<ExpressionContext> senderPredicate,
                          Function<ExpressionContext, Double> probability,
                          Mutation post
) {
}
