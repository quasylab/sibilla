package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.AgentExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.*;
import java.util.stream.Collectors;

public class AgentExpressionGenerator extends DataOrientedPopulationModelBaseVisitor<AgentExpressionFunction> {

    private final SymbolTable table;

    public AgentExpressionGenerator(SymbolTable table) {
        this.table = table;
    }

    @Override
    public AgentExpressionFunction visitAgent_expression(DataOrientedPopulationModelParser.Agent_expressionContext ctx) {
        String newSpecies = ctx.name.getText();
        int newSpeciesId = this.table.getSpeciesId(newSpecies);
        Map<String, ExpressionFunction> expressions = new HashMap<>();

        if(ctx.vars != null) {
            ExpressionGenerator expressionGenerator = new ExpressionGenerator(this.table);
            for (DataOrientedPopulationModelParser.Var_assContext vctx : ctx.vars.var_ass()) {
                expressions.put(vctx.name.getText(), vctx.expr().accept(expressionGenerator));
            }
        }

        return (context) -> {
            Map<String, SibillaValue> values = new HashMap<>();
            for(Map.Entry<String, ExpressionFunction> e : expressions.entrySet()) {
                values.put(e.getKey(), expressions.get(e.getKey()).eval(context));
            }
            return new Agent(newSpeciesId, values);
        };
    }
}
