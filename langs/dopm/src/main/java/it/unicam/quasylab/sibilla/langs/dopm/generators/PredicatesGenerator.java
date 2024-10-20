package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Predicate;

public class PredicatesGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Predicate<AgentState>>> {

    private final SymbolTable table;
    private Map<String, Predicate<AgentState>> predicates;

    public PredicatesGenerator(SymbolTable table) {
        this.table = table;
        this.predicates = new Hashtable<>();
    }

    @Override
    public Map<String, Predicate<AgentState>> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return predicates;
    }

    @Override
    public Map<String, Predicate<AgentState>> visitPredicate_declaration(DataOrientedPopulationModelParser.Predicate_declarationContext ctx) {
        predicates.put(ctx.name.getText(), getPredicateBuilder(ctx.expr()));
        return predicates;
    }

    private Predicate<AgentState> getPredicateBuilder(DataOrientedPopulationModelParser.ExprContext ctx) {
        ExpressionFunction predicateFunction = ctx.accept(new ExpressionGenerator(this.table));
        return state -> predicateFunction.eval(new ExpressionContext(Collections.emptyMap(), Collections.emptyMap(), state)) == SibillaBoolean.TRUE;
    }

    @Override
    protected Map<String, Predicate<AgentState>> defaultResult() {
        return predicates;
    }
}
