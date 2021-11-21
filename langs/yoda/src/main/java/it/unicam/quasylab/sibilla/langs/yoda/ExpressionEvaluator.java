package it.unicam.quasylab.sibilla.langs.yoda;

import java.util.function.Function;

public class ExpressionEvaluator extends YodaModelBaseVisitor<Value> {

    private final Function<String, Double> resolver;
    private final Function<String, DataType> types;

    public ExpressionEvaluator(Function<String, Double> resolver, Function<String, DataType> types) {
        super();
        this.resolver = resolver;
        this.types = types;
    }

    public static int evalInteger(Function<String, DataType> types, Function<String, Double> resolver, YodaModelParser.ExprContext exprContext){
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver,types);
        return exprContext.accept(evaluator).getIntValue();
    }
}
