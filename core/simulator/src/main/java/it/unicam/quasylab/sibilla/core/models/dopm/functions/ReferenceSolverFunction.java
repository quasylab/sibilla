package it.unicam.quasylab.sibilla.core.models.dopm.functions;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Optional;

@FunctionalInterface
public interface ReferenceSolverFunction {
    Optional<SibillaValue> solve(String name);
}
