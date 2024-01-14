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

public class AgentReceiverExpressionGenerator extends DataOrientedPopulationModelBaseVisitor<BiFunction<Agent,Agent,Agent>> {


    public AgentReceiverExpressionGenerator() {
    }

    @Override
    public BiFunction<Agent,Agent,Agent> visitAgent_expression(DataOrientedPopulationModelParser.Agent_expressionContext ctx) {
        return (sender, receiver) -> {
            String species = ctx.name.getText();
            Map<String, SibillaValue> values = new HashMap<>();
            for(Map.Entry<String, SibillaValue> e : receiver.getValues().entrySet()) {
                values.put(new String(e.getKey()), new SibillaDouble(e.getValue().doubleOf()));
            }
            if(ctx.vars != null) {
                for(DataOrientedPopulationModelParser.Var_assContext vctx : ctx.vars.var_ass()) {
                    values.put(vctx.name.getText(), vctx.expr().accept(
                            new ExpressionEvaluator(name -> {
                                if(name.contains("sender.")) {
                                    return Optional.ofNullable(sender.getValues().get(name.split("sender.")[1]));
                                } else {
                                    return Optional.ofNullable(values.get(name));
                                }
                            })
                    ));
                }
            }
            return new Agent(species, values);
        };
    }
}
