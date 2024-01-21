package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.ExpressionEvaluator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AgentReceiverExpressionGenerator extends DataOrientedPopulationModelBaseVisitor<BiFunction<Agent,Agent,Agent>> {


    public AgentReceiverExpressionGenerator() {
    }

    @Override
    public BiFunction<Agent,Agent,Agent> visitAgent_expression(DataOrientedPopulationModelParser.Agent_expressionContext ctx) {
        String species = ctx.name.getText();
        Map<String, BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue>> expressions;

        if(ctx.vars != null) {
            expressions = new HashMap<>();
            for(DataOrientedPopulationModelParser.Var_assContext vctx : ctx.vars.var_ass()) {
                expressions.put(vctx.name.getText(), vctx.expr().accept(
                        new ExpressionEvaluator()
                ));
            }
        } else {
            expressions = null;
        }


        return (sender, receiver) -> {

            Map<String, SibillaValue> values = new HashMap<>(receiver.getValues());

            if(expressions != null) {
                expressions
                        .forEach((key, value) -> values.put(key, value.apply(receiver.getResolver(), sender.getResolver())));
            }
            return new Agent(species, values);
        };
    }
}