package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.PopulationExpressionEvaluator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class RulesGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Rule>> {

    private Map<String, Rule> rules;

    public RulesGenerator() {
        this.rules = new Hashtable<>();
    }

    @Override
    public Map<String, Rule> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return rules;
    }


    @Override
    public Map<String, Rule> visitRule_declaration(DataOrientedPopulationModelParser.Rule_declarationContext ctx) {
        rules.put(ctx.name.getText(), getRuleBuilder(ctx.body));
        return rules;
    }

    private Rule getRuleBuilder(DataOrientedPopulationModelParser.Rule_bodyContext ctx) {
        OutputTransition outputTransition = new OutputTransition(
                ctx.output.pre.accept(new AgentPredicateGenerator()),
                state -> ctx.output.rate.accept(new PopulationExpressionEvaluator(state)).doubleOf(),
                ctx.output.post.accept(new AgentExpressionGenerator())
        );
        List<InputTransition> inputs = new ArrayList<>();
        for(DataOrientedPopulationModelParser.Input_transitionContext ictx : ctx.inputs.input_transition()) {
            inputs.add(new InputTransition(
                    ictx.pre.predicate.accept(new AgentPredicateGenerator()),
                    ictx.sender_predicate.accept(new AgentSenderPredicateGenerator()),
                    state -> ictx.probability.accept(new PopulationExpressionEvaluator(state)).doubleOf(),
                    ictx.post.accept(new AgentReceiverExpressionGenerator())
            ));
        }
        return new Rule(outputTransition, inputs);
    }

    @Override
    protected Map<String, Rule> defaultResult() {
        return rules;
    }
}
