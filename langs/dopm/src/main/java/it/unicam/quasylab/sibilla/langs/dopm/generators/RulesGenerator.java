package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.*;

public class RulesGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Rule>> {

    private final SymbolTable table;
    private final Map<String, Rule> rules;

    public RulesGenerator(SymbolTable table) {
        this.table = table;
        this.rules = new Hashtable<>();
    }

    @Override
    public Map<String, Rule> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return rules;
    }


    @Override
    public Map<String, Rule> visitRule_declaration(DataOrientedPopulationModelParser.Rule_declarationContext ctx) {
        rules.put(ctx.name.getText(), getRule(ctx.name.getText(), ctx.body));
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
        return new Rule(ruleName, outputTransition, inputs);
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
    protected Map<String, Rule> defaultResult() {
        return rules;
    }
}