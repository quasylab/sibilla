package it.unicam.quasylab.sibilla.core.models.dopm.functions;

import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

@FunctionalInterface
public interface ExpressionFunction {
    SibillaValue eval(ReferenceSolverFunction agent, ReferenceSolverFunction sender, DataOrientedPopulationState state);
}
