package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.ExpressionEvaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AgentExpressionGenerator extends DataOrientedPopulationModelBaseVisitor<Function<Agent,Agent>> {


    public AgentExpressionGenerator() {
    }

    @Override
    public Function<Agent,Agent> visitAgent_expression(DataOrientedPopulationModelParser.Agent_expressionContext ctx) {
        return a -> {
            String species = ctx.name.getText();
            Map<String, SibillaValue> values = new HashMap<>(a.getValues());
            for(DataOrientedPopulationModelParser.Var_assContext vctx : ctx.vars.var_ass()) {
                values.put(vctx.name.getText(), vctx.expr().accept(new ExpressionEvaluator(name -> Optional.ofNullable(values.get(name)))));
            }
            return new Agent(species, values);
        };
    }
}
