package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;

import java.util.*;

public class MeasuresGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Measure<DataOrientedPopulationState>>> {

    private final SymbolTable table;
    private final Map<String, Measure<DataOrientedPopulationState>> measures;

    public MeasuresGenerator(SymbolTable table) {
        this.table = table;
        this.measures = new Hashtable<>();
    }

    @Override
    public Map<String, Measure<DataOrientedPopulationState>> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return measures;
    }


    @Override
    public Map<String, Measure<DataOrientedPopulationState>> visitMeasure_declaration(DataOrientedPopulationModelParser.Measure_declarationContext ctx) {
        measures.put(ctx.name.getText(), getMeasureBuilder(ctx));
        return measures;
    }

    private Measure<DataOrientedPopulationState> getMeasureBuilder(DataOrientedPopulationModelParser.Measure_declarationContext ctx) {
        ExpressionFunction measurefunction = ctx.expr().accept(new ExpressionGenerator(this.table, null, null));

        return new SimpleMeasure<DataOrientedPopulationState>(
                ctx.name.getText(),
                state -> measurefunction.eval(new ExpressionContext(Collections.emptyList(), Collections.emptyList(), state)).doubleOf()
        );
    }

    @Override
    protected Map<String, Measure<DataOrientedPopulationState>> defaultResult() {
        return measures;
    }
}
