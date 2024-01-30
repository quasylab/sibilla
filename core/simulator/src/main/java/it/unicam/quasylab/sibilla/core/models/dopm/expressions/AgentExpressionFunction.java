package it.unicam.quasylab.sibilla.core.models.dopm.expressions;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;

@FunctionalInterface
public interface AgentExpressionFunction {
    Agent eval(ExpressionContext context);
}
