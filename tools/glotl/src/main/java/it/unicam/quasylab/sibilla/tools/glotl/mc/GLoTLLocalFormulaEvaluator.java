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

package it.unicam.quasylab.sibilla.tools.glotl.mc;

import it.unicam.quasylab.sibilla.core.models.lio.LIOAgent;
import it.unicam.quasylab.sibilla.core.models.lio.LIOMixedState;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.tools.glotl.local.LocalFormula;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class GLoTLLocalFormulaEvaluator implements GLoTLbLocalPathProbabilityEvaluator {

    private final LocalFormula<LIOAgent> formula;

    private final Map<Pair<LIOMixedState, LocalFormula<LIOAgent>>, Double> cache;

    public GLoTLLocalFormulaEvaluator(LocalFormula<LIOAgent> formula) {
        this.formula = formula;
        cache = new HashMap<>();
    }


    @Override
    public double eval(LIOMixedState state) {
        if (formula.isAccepting()) {
            return 1.0;
        }
        if (formula.isRejecting()) {
            return 0.0;
        }
        Pair<LIOMixedState, LocalFormula<LIOAgent>> element = new Pair<>(state, formula);
        return compute(element);
    }

    private double compute(Pair<LIOMixedState, LocalFormula<LIOAgent>> element) {
        Queue<PendingElement> elements = new LinkedList<>();
        elements.add(new PendingElement(element));
        double sum = 0.0;
        while (elements.isEmpty()) {
            PendingElement pe = elements.poll();
            if (pe.vector == null) {
                if (pe.element.getValue().isAccepting()) {
                    cache.put(pe.element, 1.0);
                }
                if (pe.element.getValue().isRejecting()) {
                    cache.put(pe.element, 0.0);
                }
                addToQueueIfNotInCache(pe.element, elements);
            } else {
                cache.put(pe.element, pe.compute());
            }

        }
        return sum;
    }

    private void addToQueueIfNotInCache(Pair<LIOMixedState, LocalFormula<LIOAgent>> element, Queue<PendingElement> elements) {

    }


    private class PendingElement {

        private final Pair<LIOMixedState, LocalFormula<LIOAgent>> element;

        private ProbabilityVector<Pair<LIOMixedState, LocalFormula<LIOAgent>>> vector;

        private PendingElement(Pair<LIOMixedState, LocalFormula<LIOAgent>> element) {
            this.element = element;
        }

        public double compute() {
            return vector.compute(p -> cache.getOrDefault(p, 0.0));
        }
    }

}
