package it.unicam.quasylab.sibilla.langs.enba.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Predicate;

public class PredicatesGenerator extends ExtendedNBABaseVisitor<Map<String, Predicate<AgentState>>> {

    private final SymbolTable table;
    private Map<String, Predicate<AgentState>> predicates;

    public PredicatesGenerator(SymbolTable table) {
        this.table = table;
        this.predicates = new Hashtable<>();
    }

    @Override
    public Map<String, Predicate<AgentState>> visitModel(ExtendedNBAParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return predicates;
    }

    @Override
    public Map<String, Predicate<AgentState>> visitPredicate_declaration(ExtendedNBAParser.Predicate_declarationContext ctx) {
        predicates.put(ctx.name.getText(), getPredicateBuilder(ctx.expr()));
        return predicates;
    }

    private Predicate<AgentState> getPredicateBuilder(ExtendedNBAParser.ExprContext ctx) {
        ExpressionFunction predicateFunction = ctx.accept(new ExpressionGenerator(this.table, null, null));
        return state -> predicateFunction.eval(new ExpressionContext(Collections.emptyList(), Collections.emptyList(), state)) == SibillaBoolean.TRUE;
    }

    @Override
    protected Map<String, Predicate<AgentState>> defaultResult() {
        return predicates;
    }
}
