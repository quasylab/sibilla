package it.unicam.quasylab.sibilla.langs.dopm.evaluators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.generators.AgentPredicateGenerator;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class PopulationExpressionEvaluator extends ExpressionEvaluator{
    private final DataOrientedPopulationState state;
    public PopulationExpressionEvaluator(DataOrientedPopulationState state) {
        super(n -> Optional.empty());
        this.state = state;
    }

    public PopulationExpressionEvaluator(DataOrientedPopulationState state, Function<String, Optional<SibillaValue>> resolver) {
        super(resolver);
        this.state = state;
    }


    @Override
    public SibillaValue visitPopulationFractionExpression(DataOrientedPopulationModelParser.PopulationFractionExpressionContext ctx) {
        Predicate<Agent> predicate = new AgentPredicateGenerator().visitAgent_predicate(ctx.agent_predicate());
        return SibillaValue.of(this.state.fractionOf(predicate));
    }

    @Override
    public SibillaValue visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        Predicate<Agent> predicate = new AgentPredicateGenerator().visitAgent_predicate(ctx.agent_predicate());
        return SibillaValue.of(this.state.numberOf(predicate));
    }

}
