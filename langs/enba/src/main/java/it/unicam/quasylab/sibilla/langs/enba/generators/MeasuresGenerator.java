package it.unicam.quasylab.sibilla.langs.enba.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class MeasuresGenerator extends ExtendedNBABaseVisitor<Map<String, Measure<AgentState>>> {

    private final SymbolTable table;
    private final Map<String, Measure<AgentState>> measures;

    public MeasuresGenerator(SymbolTable table) {
        this.table = table;
        this.measures = new Hashtable<>();
    }

    @Override
    public Map<String, Measure<AgentState>> visitModel(ExtendedNBAParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return measures;
    }


    @Override
    public Map<String, Measure<AgentState>> visitMeasure_declaration(ExtendedNBAParser.Measure_declarationContext ctx) {
        measures.put(ctx.name.getText(), getMeasureBuilder(ctx));
        return measures;
    }

    private Measure<AgentState> getMeasureBuilder(ExtendedNBAParser.Measure_declarationContext ctx) {
        ExpressionFunction measureFunction = ctx.expr().accept(new ExpressionGenerator(this.table, null, null));

        return new SimpleMeasure<AgentState>(
                ctx.name.getText(),
                state -> measureFunction.eval(new ExpressionContext(Collections.emptyList(), Collections.emptyList(), state)).doubleOf()
        );
    }

    @Override
    protected Map<String, Measure<AgentState>> defaultResult() {
        return measures;
    }
}
