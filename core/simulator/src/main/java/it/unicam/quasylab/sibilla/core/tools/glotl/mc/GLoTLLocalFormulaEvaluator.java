/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.tools.glotl.mc;

import it.unicam.quasylab.sibilla.core.models.lio.Agent;
import it.unicam.quasylab.sibilla.core.models.lio.LIOMixedState;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalFormula;
import it.unicam.quasylab.sibilla.core.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class GLoTLLocalFormulaEvaluator implements GLoTLbLocalPathProbabilityEvaluator {

    private final LocalFormula<Agent> formula;

    private final Map<Pair<LIOMixedState, LocalFormula<Agent>>, Double> cache;

    public GLoTLLocalFormulaEvaluator(LocalFormula<Agent> formula) {
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
        Pair<LIOMixedState,LocalFormula<Agent>> element = new Pair<>(state, formula);
        return compute(element);
    }

    private double compute(Pair<LIOMixedState, LocalFormula<Agent>> element) {
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

    private void addToQueueIfNotInCache(Pair<LIOMixedState, LocalFormula<Agent>> element, Queue<PendingElement> elements) {
        LocalFormula<Agent> nextFormula = element.getValue().next(element.getKey().getAgent());
        ProbabilityVector<LIOMixedState> vector = element.getKey().next();
    }


    private class PendingElement {

        private final Pair<LIOMixedState, LocalFormula<Agent>> element;

        private ProbabilityVector<Pair<LIOMixedState, LocalFormula<Agent>>> vector;

        private PendingElement(Pair<LIOMixedState, LocalFormula<Agent>> element) {
            this.element = element;
        }

        public double compute() {
            return vector.compute(p -> cache.getOrDefault(p, 0.0));
        }
    }

}
