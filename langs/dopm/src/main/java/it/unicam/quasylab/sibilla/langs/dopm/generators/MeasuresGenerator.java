package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.PopulationExpressionEvaluator;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.TriFunction;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Function;

public class MeasuresGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Measure<DataOrientedPopulationState>>> {

    private Map<String, Measure<DataOrientedPopulationState>> measures;

    public MeasuresGenerator() {
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
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> measurefunction = ctx.expr().accept(new PopulationExpressionEvaluator());

        return new SimpleMeasure<DataOrientedPopulationState>(
                ctx.name.getText(),
                state -> measurefunction.apply(n -> Optional.empty(), n-> Optional.empty(), state).doubleOf()
        );
    }

    @Override
    protected Map<String, Measure<DataOrientedPopulationState>> defaultResult() {
        return measures;
    }
}
