package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.ExpressionEvaluator;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AgentExpressionGenerator extends DataOrientedPopulationModelBaseVisitor<Function<Agent,Agent>> {


    public AgentExpressionGenerator() {
    }

    @Override
    public Function<Agent,Agent> visitAgent_expression(DataOrientedPopulationModelParser.Agent_expressionContext ctx) {
        String species = ctx.name.getText();
        Map<String, BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue>> expressions;

        if(ctx.vars != null) {
            expressions = new HashMap<>();
            for (DataOrientedPopulationModelParser.Var_assContext vctx : ctx.vars.var_ass()) {
                expressions.put(vctx.name.getText(), vctx.expr().accept(new ExpressionEvaluator()));
            }
        } else {
            expressions = null;
        }

        return a -> {
            Map<String, SibillaValue> values = new HashMap<>(a.getValues());
            if(expressions != null) {
                expressions.forEach((key,value) -> values.put(key, value.apply(a.getResolver(), name -> Optional.empty())));
            }
            return new Agent(species, values);
        };
    }
}
