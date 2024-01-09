package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.ExpressionEvaluator;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.function.Predicate;

public class AgentPredicateGenerator extends DataOrientedPopulationModelBaseVisitor<Predicate<Agent>> {


    public AgentPredicateGenerator() {
    }

    @Override
    public Predicate<Agent> visitAgent_predicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx) {
        return a -> {
            if(!a.getSpecies().equals(ctx.name.getText())) {
                return false;
            }
            for(TerminalNode node : ctx.vars.ID()) {
                if (!a.getValues().containsKey(node.getText())) {
                    return false;
                }
            }
            return ctx.predicate.accept(new ExpressionEvaluator(name -> Optional.ofNullable(a.getValues().get(name)))) == SibillaBoolean.TRUE;
        };
    }
}
