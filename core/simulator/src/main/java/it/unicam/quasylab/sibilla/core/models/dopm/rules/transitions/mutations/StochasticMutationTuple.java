package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.mutations;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.AgentExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionFunction;

public record StochasticMutationTuple(AgentExpressionFunction agentExpression, ExpressionFunction probability) {
}
