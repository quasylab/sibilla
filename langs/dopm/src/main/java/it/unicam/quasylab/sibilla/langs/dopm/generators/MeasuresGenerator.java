package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.*;

public class MeasuresGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Measure<AgentState>>> {

    private final SymbolTable table;
    private final Map<String, Measure<AgentState>> measures;

    public MeasuresGenerator(SymbolTable table) {
        this.table = table;
        this.measures = new Hashtable<>();
    }

    @Override
    public Map<String, Measure<AgentState>> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return measures;
    }


    @Override
    public Map<String, Measure<AgentState>> visitMeasure_declaration(DataOrientedPopulationModelParser.Measure_declarationContext ctx) {
        measures.put(ctx.name.getText(), getMeasureBuilder(ctx));
        return measures;
    }

    private Measure<AgentState> getMeasureBuilder(DataOrientedPopulationModelParser.Measure_declarationContext ctx) {
        ExpressionFunction measurefunction = ctx.expr().accept(new ExpressionGenerator(this.table));

        return new SimpleMeasure<AgentState>(
                ctx.name.getText(),
                state -> measurefunction.eval(new ExpressionContext(Collections.emptyMap(), Collections.emptyMap(), state)).doubleOf()
        );
    }

    @Override
    protected Map<String, Measure<AgentState>> defaultResult() {
        return measures;
    }
}
