package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
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
        rules.add(getRule(ctx.name.getText(), ctx.body));
        return rules;
    }

    private Rule getRule(String ruleName, DataOrientedPopulationModelParser.Rule_bodyContext ctx) {
        String species = ctx.output.pre.name.getText();
        ExpressionFunction outRate = ctx.output.rate.accept(new ExpressionGenerator(this.table, species, null));
        OutputTransition outputTransition = new OutputTransition(
                ctx.output.pre.accept(new AgentPredicateGenerator(this.table)),
                (context) -> outRate.eval(context).doubleOf(),
                ctx.output.post.accept(new AgentMutationGenerator(this.table, species, null))
        );
        List<InputTransition> inputs = getInputs(species, ctx.inputs);
        return new Rule(outputTransition, inputs);
    }

    private List<InputTransition> getInputs(String senderSpecies, DataOrientedPopulationModelParser.Input_transition_listContext transitionList) {
        List<InputTransition> inputs = new ArrayList<>();
        for(DataOrientedPopulationModelParser.Input_transitionContext ictx : transitionList.input_transition()) {
            String species = ictx.pre.name.getText();
            ExpressionFunction senderPredicate = ictx.sender_predicate.accept(new ExpressionGenerator(this.table, senderSpecies, null));
            ExpressionFunction probability = ictx.probability.accept(new ExpressionGenerator(this.table, species, null));
            inputs.add(new InputTransition(
                    ictx.pre.accept(new AgentPredicateGenerator(this.table)),
                    context -> senderPredicate.eval(context) == SibillaBoolean.TRUE,
                    context -> probability.eval(context).doubleOf(),
                    ictx.post.accept(new AgentMutationGenerator(this.table, species, senderSpecies))
            ));
        }
        return inputs;
    }


    @Override
    protected List<Rule> defaultResult() {
        return rules;
    }
}