package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.AgentExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.*;
import java.util.stream.Collectors;

public class AgentExpressionGenerator extends DataOrientedPopulationModelBaseVisitor<AgentExpressionFunction> {

    private final SymbolTable table;
    private final String agentSpecies;
    private final String senderSpecies;

    public AgentExpressionGenerator(SymbolTable table, String agentSpecies, String senderSpecies) {
        this.table = table;
        this.agentSpecies = agentSpecies;
        this.senderSpecies = senderSpecies;
    }

    @Override
    public AgentExpressionFunction visitAgent_expression(DataOrientedPopulationModelParser.Agent_expressionContext ctx) {
        String newSpecies = ctx.name.getText();
        int newSpeciesId = this.table.getSpeciesId(newSpecies);
        List<ExpressionFunction> expressions = new ArrayList<>();

        if(ctx.vars != null) {
            ExpressionGenerator expressionGenerator = new ExpressionGenerator(this.table, agentSpecies, senderSpecies);
            for (DataOrientedPopulationModelParser.Var_assContext vctx : ctx.vars.var_ass()) {
                expressions.add(vctx.expr().accept(expressionGenerator));
            }
        }

        return (context) -> new Agent(
                newSpeciesId,
                expressions.stream()
                        .map(expr -> expr.eval(context))
                        .collect(Collectors.toList())
        );
    }
}
