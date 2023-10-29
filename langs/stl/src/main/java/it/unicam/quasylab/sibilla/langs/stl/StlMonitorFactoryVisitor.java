/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.langs.stl;

import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.langs.slam.StlModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.slam.StlModelParser;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.ParseError;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class StlMonitorFactoryVisitor<S> extends StlModelBaseVisitor<Boolean> {
    private final Map<String, ToDoubleFunction<S>> measures;
    private final ErrorCollector errors;
    private final StlMonitorFactory<S> monitorFactory = new StlMonitorFactory<>();

    private String[] declaredMeasures;

    public StlMonitorFactoryVisitor(ErrorCollector errors, Map<String, ToDoubleFunction<S>> measures) {
        this.measures = measures;
        this.errors = errors;
    }


    public StlMonitorFactory<S> getFactory() {
        return monitorFactory;
    }

    @Override
    public Boolean visitModel(StlModelParser.ModelContext ctx) {
        if (checkDeclaredMeasures(ctx.declarationMeasure())) {
            boolean flag = true;
            for (StlModelParser.DeclarationFormulaContext declarationFormulaContext : ctx.declarationFormula()) {
                flag &= declarationFormulaContext.accept(this);
            }
            return flag;
        } else {
            return false;
        }
    }

    private boolean checkDeclaredMeasures(List<StlModelParser.DeclarationMeasureContext> declarationMeasureContexts) {
        StlModelParser.DeclarationMeasureContext[] undefinedMeasures = declarationMeasureContexts.stream().filter(dm -> !measures.containsKey(dm.getText())).toArray(StlModelParser.DeclarationMeasureContext[]::new);
        if (undefinedMeasures.length != 0) {
            Arrays.stream(undefinedMeasures).forEach(dm -> errors.record(new ParseError(String.format("Measure %s is not defined in the module", dm.name.getText()), dm.name.getLine(), dm.name.getCharPositionInLine())));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Boolean visitDeclarationFormula(StlModelParser.DeclarationFormulaContext ctx) {
        monitorFactory.addMonitor(ctx.name.getText(), ctx.params.stream().map(p -> p.name.getText()).toArray(String[]::new), getQualitativeMonitor(ctx.formula), getQuantitativeMonitor(ctx.formula));
        return super.visitDeclarationFormula(ctx);
    }

    private Function<Map<String, Double>, QuantitativeMonitor<S>> getQuantitativeMonitor(StlModelParser.StlFormulaContext formula) {
        return formula.accept(new StlQuantitativeMonitorEvaluator<>(measures));
    }

    private Function<Map<String, Double>, QualitativeMonitor<S>> getQualitativeMonitor(StlModelParser.StlFormulaContext formula) {
        return formula.accept(new StlQualitativeMonitorEvaluator<>(measures));
    }
}
