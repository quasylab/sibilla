package it.unicam.quasylab.sibilla.langs.enba.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;

import java.util.function.BiPredicate;

public class AgentPredicateGenerator extends ExtendedNBABaseVisitor<BiPredicate<Integer,ExpressionContext>> {
    private final SymbolTable table;

    public AgentPredicateGenerator(SymbolTable table) {
        this.table = table;
    }

   @Override
    public BiPredicate<Integer, ExpressionContext> visitAgent_predicate(ExtendedNBAParser.Agent_predicateContext ctx) {
        String predicateSpecies = ctx.name.getText();
        int predicateSpeciesId = this.table.getSpeciesId(predicateSpecies);
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(this.table);
        ExpressionFunction predicate = ctx.predicate.accept(expressionGenerator);
        return (species, context) -> {
            if(species != predicateSpeciesId) {
                return false;
            }
            return predicate.eval(context) == SibillaBoolean.TRUE;
        };
    }
}