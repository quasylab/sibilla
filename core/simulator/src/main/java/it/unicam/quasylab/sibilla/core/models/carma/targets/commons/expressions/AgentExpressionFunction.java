package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;

@FunctionalInterface
public interface AgentExpressionFunction {
    Agent eval(ExpressionContext context);
}
