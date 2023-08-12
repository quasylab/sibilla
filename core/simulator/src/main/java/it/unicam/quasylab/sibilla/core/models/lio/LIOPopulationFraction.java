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

package it.unicam.quasylab.sibilla.core.models.lio;

import it.unicam.quasylab.sibilla.core.tools.ProbabilityMatrix;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class LIOPopulationFraction implements LIOCollective {

    private final double[] populationFraction;
    private final LIOAgentDefinitions agentsDefinition;

    protected LIOPopulationFraction(LIOAgentDefinitions agentsDefinition, double[] populationFraction) {
        this.agentsDefinition = agentsDefinition;
        this.populationFraction = populationFraction;
    }

    @Override
    public double fractionOf(LIOAgent a) {
        return populationFraction[a.getIndex()];
    }

    @Override
    public double fractionOf(Predicate<LIOAgent> predicate) {
        return IntStream.range(0, populationFraction.length)
                .filter(i -> predicate.test(agentsDefinition.getAgent(i)))
                .mapToDouble(i -> populationFraction[i]).sum();
    }

    @Override
    public Set<LIOAgent> getAgents() {
        return agentsDefinition.getAgents(IntStream.range(0, populationFraction.length).filter(i -> i>0).toArray());
    }

    public LIOPopulationFraction multuply(ProbabilityMatrix<LIOAgent> matrix) {
        double[] result = new double[populationFraction.length];
        matrix.iterate((a1, pv) -> pv.iterate((a2,p) -> {
                result[a2.getIndex()] += populationFraction[a1.getIndex()]*p;
            }
        ));
        return new LIOPopulationFraction(this.agentsDefinition, result);
    }

}
