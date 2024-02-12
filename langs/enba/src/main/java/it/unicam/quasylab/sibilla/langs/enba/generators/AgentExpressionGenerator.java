package it.unicam.quasylab.sibilla.langs.enba.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.AgentExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AgentExpressionGenerator extends ExtendedNBABaseVisitor<AgentExpressionFunction> {

    private final SymbolTable table;
    private final String agentSpecies;
    private final String senderSpecies;

    public AgentExpressionGenerator(SymbolTable table, String agentSpecies, String senderSpecies) {
        this.table = table;
        this.agentSpecies = agentSpecies;
        this.senderSpecies = senderSpecies;
    }

    @Override
    public AgentExpressionFunction visitAgent_expression(ExtendedNBAParser.Agent_expressionContext ctx) {
        String newSpecies = ctx.name.getText();
        int newSpeciesId = this.table.getSpeciesId(newSpecies);
        List<ExpressionFunction> expressions = new ArrayList<>();

        if(ctx.vars != null) {
            ExpressionGenerator expressionGenerator = new ExpressionGenerator(this.table, agentSpecies, senderSpecies);
            for (ExtendedNBAParser.Var_assContext vctx : ctx.vars.var_ass()) {
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
