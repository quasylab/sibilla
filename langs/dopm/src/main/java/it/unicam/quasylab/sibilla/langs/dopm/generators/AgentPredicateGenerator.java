package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class AgentPredicateGenerator extends DataOrientedPopulationModelBaseVisitor<BiPredicate<Integer,ExpressionContext>> {
    private final SymbolTable table;

    public AgentPredicateGenerator(SymbolTable table) {
        this.table = table;
    }

   @Override
    public BiPredicate<Integer, ExpressionContext> visitAgent_predicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx) {
        String predicateSpecies = ctx.name.getText();
        int predicateSpeciesId = this.table.getSpeciesId(predicateSpecies);
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(
                this.table,
                predicateSpecies,
                null
        );
        ExpressionFunction predicate = ctx.predicate.accept(expressionGenerator);
        return (species, context) -> {
            if(species != predicateSpeciesId) {
                return false;
            }
            return predicate.eval(context) == SibillaBoolean.TRUE;
        };
    }
}