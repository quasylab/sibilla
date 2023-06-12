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
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class LIOMeanFieldState implements LIOState<LIOMeanFieldState> {

    private final AgentsDefinition definition;

    private final double[] fractionsOfAgents;

    public LIOMeanFieldState(AgentsDefinition definition, double[] fractionsOfAgents) {
        if (definition.numberOfAgents() != fractionsOfAgents.length) {
            throw new IllegalArgumentException();
        }
        this.definition = definition;
        this.fractionsOfAgents = fractionsOfAgents;
    }

    @Override
    public double size() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean isInfinite() {
        return true;
    }

    @Override
    public double fractionOf(Agent a) {
        return fractionsOfAgents[a.getIndex()];
    }

    @Override
    public double fractionOf(Predicate<Agent> predicate) {
        return IntStream.range(0,fractionsOfAgents.length).filter(i -> predicate.test(definition.getAgent(i))).mapToDouble(i -> fractionsOfAgents[i]).sum();
    }


    @Override
    public double numberOf(Agent a) {
        return Double.NaN;
    }

    @Override
    public double numberOf(Predicate<Agent> predicate) {
        return Double.NaN;
    }

    @Override
    public LIOMeanFieldState step(RandomGenerator randomGenerator, ProbabilityMatrix<Agent> matrix) {
        return step(null, matrix);
    }

    public LIOMeanFieldState step(ProbabilityMatrix<Agent> matrix) {
        double[] nextFraction = new double[fractionsOfAgents.length];
        for(int i=0; i<nextFraction.length; i++) {
            ProbabilityVector<Agent> row = matrix.getRowOf(definition.getAgent(i));
            row.iterate((a,v) -> nextFraction[a.getIndex()] += v);
        }
        return new LIOMeanFieldState(definition, nextFraction);
    }

    @Override
    public ProbabilityVector<LIOMeanFieldState> next(ProbabilityMatrix<Agent> matrix) {
        return ProbabilityVector.dirac(step(matrix));
    }

    @Override
    public ProbabilityVector<LIOMeanFieldState> next() {
        return next(definition.getAgentProbabilityMatrix(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LIOMeanFieldState that = (LIOMeanFieldState) o;
        return Arrays.equals(fractionsOfAgents, that.fractionsOfAgents);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(fractionsOfAgents);
    }
}
