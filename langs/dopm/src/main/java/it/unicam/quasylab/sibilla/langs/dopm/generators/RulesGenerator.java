package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.UnicastRule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.BroadcastRule;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.*;

public class RulesGenerator extends DataOrientedPopulationModelBaseVisitor<List<Rule>> {

    private final SymbolTable table;
    private final List<Rule> rules;

    public RulesGenerator(SymbolTable table) {
        this.table = table;
        this.rules = new ArrayList<>();
    }

    @Override
    public List<Rule> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return rules;
    }


    @Override
    public List<Rule> visitRule_declaration(DataOrientedPopulationModelParser.Rule_declarationContext ctx) {
        rules.add(getRule(ctx.body));
        return rules;
    }

    private Rule getRule(DataOrientedPopulationModelParser.Rule_bodyContext ctx) {
        boolean broadcastRule = ctx.broadcast_rule_body() != null;

        DataOrientedPopulationModelParser.Output_transitionContext output = broadcastRule
                ? ctx.broadcast_rule_body().output
                : ctx.unicast_rule_body().output;

        DataOrientedPopulationModelParser.Input_transition_listContext inputs = broadcastRule
                ? ctx.broadcast_rule_body().inputs
                : ctx.unicast_rule_body().inputs;

        ExpressionFunction outRate = output.rate.accept(new ExpressionGenerator(this.table));
        OutputTransition outputTransition = new OutputTransition(
                output.pre.accept(new AgentPredicateGenerator(this.table)),
                (context) -> outRate.eval(context).doubleOf(),
                output.post.accept(new AgentMutationGenerator(this.table))
        );
        List<InputTransition> inputTransitions = getInputTransitions(inputs);

        return broadcastRule
                ? new BroadcastRule(outputTransition, inputTransitions)
                : new UnicastRule(outputTransition, inputTransitions);
    }

    private List<InputTransition> getInputTransitions(DataOrientedPopulationModelParser.Input_transition_listContext transitionList) {
        List<InputTransition> inputs = new ArrayList<>();
        for(DataOrientedPopulationModelParser.Input_transitionContext ictx : transitionList.input_transition()) {
            ExpressionFunction senderPredicate = ictx.sender_predicate.accept(new ExpressionGenerator(this.table));
            ExpressionFunction probability = ictx.probability.accept(new ExpressionGenerator(this.table));
            inputs.add(new InputTransition(
                    ictx.pre.accept(new AgentPredicateGenerator(this.table)),
                    context -> senderPredicate.eval(context) == SibillaBoolean.TRUE,
                    context -> probability.eval(context).doubleOf(),
                    ictx.post.accept(new AgentMutationGenerator(this.table))
            ));
        }
        return inputs;
    }


    @Override
    protected List<Rule> defaultResult() {
        return rules;
    }
}