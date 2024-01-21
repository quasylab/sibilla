package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.ExpressionEvaluator;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class AgentPredicateGenerator extends DataOrientedPopulationModelBaseVisitor<Predicate<Agent>> {


    public AgentPredicateGenerator() {
    }

    @Override
    public Predicate<Agent> visitAgent_predicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx) {
        String predicateSpecies = ctx.name.getText();
        List<String> vars;

        if(ctx.vars != null) {
            vars = new ArrayList<>();
            for (TerminalNode node : ctx.vars.ID()) {
                vars.add(node.getText());
            }
        } else{
            vars = null;
        }

        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> predicate = ctx.predicate.accept(new ExpressionEvaluator());

        return a -> {
            if(!a.getSpecies().equals(predicateSpecies)) {
                return false;
            }
            if(vars != null) {
                for (String var : vars) {
                    if (!a.getValues().containsKey(var)) {
                        return false;
                    }
                }
            }
            return predicate.apply(a.getResolver(), n -> Optional.empty()) == SibillaBoolean.TRUE;
        };
    }
}