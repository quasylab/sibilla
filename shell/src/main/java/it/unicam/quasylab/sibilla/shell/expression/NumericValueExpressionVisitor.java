package it.unicam.quasylab.sibilla.shell.expression;

import it.unicam.quasylab.sibilla.shell.SibillaScriptBaseVisitor;
import it.unicam.quasylab.sibilla.shell.SibillaScriptParser;


public class NumericValueExpressionVisitor extends SibillaScriptBaseVisitor<Double> {

    @Override
    public Double visitIntValue(SibillaScriptParser.IntValueContext ctx) {
        return Double.parseDouble(ctx.getText());
    }

    @Override
    public Double visitRealValue(SibillaScriptParser.RealValueContext ctx) {
        return Double.parseDouble(ctx.getText());
    }

    @Override
    public Double visitInfinity(SibillaScriptParser.InfinityContext ctx) {
        return ctx.getText().equals("-inf") ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    }

}
