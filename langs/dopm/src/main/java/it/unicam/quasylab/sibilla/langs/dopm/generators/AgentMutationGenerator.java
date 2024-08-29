package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.DeterministicMutation;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.Mutation;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.StochasticMutation;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.StochasticMutationTuple;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.List;

public class AgentMutationGenerator extends DataOrientedPopulationModelBaseVisitor<Mutation> {
    private final SymbolTable table;

    public AgentMutationGenerator(SymbolTable table) {
        this.table = table;
    }

    @Override
    public Mutation visitAgent_mutation(DataOrientedPopulationModelParser.Agent_mutationContext ctx) {
        return ctx.deterministic_mutation != null
               ? getDeterministicMutation(ctx.deterministic_mutation)
               : getStochasticMutation(ctx.stochastic_mutation_tuple());
    }

    private DeterministicMutation getDeterministicMutation(DataOrientedPopulationModelParser.Agent_expressionContext ctx) {
        return new DeterministicMutation(
                ctx.accept(
                        new AgentExpressionGenerator(this.table)
                )
        );
    }

    private StochasticMutation getStochasticMutation(List<DataOrientedPopulationModelParser.Stochastic_mutation_tupleContext> tuples) {
        AgentExpressionGenerator agentExpressionGenerator = new AgentExpressionGenerator(this.table);
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(this.table);
        return new StochasticMutation(
                tuples.stream().map(
                        tctx -> new StochasticMutationTuple
                        (
                                tctx.agent_expression().accept(agentExpressionGenerator),
                                tctx.expr().accept(expressionGenerator)
                        )
                ).toList()
        );
    }
}