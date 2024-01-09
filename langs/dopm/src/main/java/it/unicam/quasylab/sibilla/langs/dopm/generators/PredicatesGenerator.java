package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.PopulationExpressionEvaluator;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Predicate;

public class PredicatesGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Predicate<DataOrientedPopulationState>>> {

    private Map<String, Predicate<DataOrientedPopulationState>> predicates;

    public PredicatesGenerator() {
        this.predicates = new Hashtable<>();
    }

    @Override
    public Map<String, Predicate<DataOrientedPopulationState>> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return predicates;
    }

    @Override
    public Map<String, Predicate<DataOrientedPopulationState>> visitPredicate_declaration(DataOrientedPopulationModelParser.Predicate_declarationContext ctx) {
        predicates.put(ctx.name.getText(), getPredicateBuilder(ctx.expr()));
        return predicates;
    }

    private Predicate<DataOrientedPopulationState> getPredicateBuilder(DataOrientedPopulationModelParser.ExprContext ctx) {
        return state -> ctx.accept(new PopulationExpressionEvaluator(state)) == SibillaBoolean.TRUE;
    }

    @Override
    protected Map<String, Predicate<DataOrientedPopulationState>> defaultResult() {
        return predicates;
    }
}
