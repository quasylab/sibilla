package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.ExpressionEvaluator;

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
            Map<String, SibillaValue> values = new HashMap<>(receiver.getValues());
            for(DataOrientedPopulationModelParser.Var_assContext vctx : ctx.vars.var_ass()) {
                values.put(vctx.name.getText(), vctx.expr().accept(
                        new ExpressionEvaluator(name -> {
                            if(name.startsWith("sender.")) {
                                return Optional.ofNullable(sender.getValues().get(name.split("sender.")[0]));
                            } else {
                                return Optional.ofNullable(receiver.getValues().get(name));
                            }
                        })
                ));
            }
            return new Agent(species, values);
        };
    }
}
