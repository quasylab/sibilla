package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.AgentExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;

public record StochasticMutationTuple(AgentExpressionFunction agentExpression, ExpressionFunction probability) {
}
