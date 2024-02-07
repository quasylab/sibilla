package it.unicam.quasylab.sibilla.core.models.dopm.expressions;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

@FunctionalInterface
public interface ExpressionFunction {
    SibillaValue eval(ExpressionContext context);
}
