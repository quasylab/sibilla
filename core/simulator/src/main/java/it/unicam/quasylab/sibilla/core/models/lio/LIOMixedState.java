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

import java.util.function.Predicate;

public class LIOMixedState<S extends LIOState<S>> implements LIOState<LIOMixedState<S>> {

    private final AgentsDefinition definition;

    private final Agent thisAgent;

    private final S otherAgents;

    public LIOMixedState(AgentsDefinition definition, Agent thisAgent, S otherAgents) {
        this.definition = definition;
        this.thisAgent = thisAgent;
        this.otherAgents = otherAgents;
    }

    @Override
    public double size() {
        if (this.otherAgents.isInfinite()) {
            return this.otherAgents.size();
        }
        return 1+this.otherAgents.size();
    }

    @Override
    public double fractionOf(Agent a) {
        if (this.otherAgents.isInfinite()||!this.thisAgent.equals(a)) {
            return this.otherAgents.fractionOf(a);
        } else {
            double size = this.otherAgents.size();
            return (otherAgents.fractionOf(a)*size+1)/(size+1);
        }
    }

    @Override
    public double fractionOf(Predicate<Agent> predicate) {
        if (this.otherAgents.isInfinite()||!predicate.test(thisAgent)) {
            return this.otherAgents.fractionOf(predicate);
        } else {
            double size = this.otherAgents.size();
            return (this.otherAgents.fractionOf(predicate)*size+1)/(size+1);
        }
    }

    @Override
    public double numberOf(Agent a) {
        if (this.otherAgents.isInfinite()||(!thisAgent.equals(a))) {
            return this.otherAgents.numberOf(a);
        } else {
            return 1+this.otherAgents.numberOf(a);
        }
    }

    @Override
    public double numberOf(Predicate<Agent> predicate) {
        if (this.otherAgents.isInfinite()||(!predicate.test(thisAgent))) {
            return this.otherAgents.numberOf(predicate);
        } else {
            return 1+this.otherAgents.numberOf(predicate);
        }
    }

    @Override
    public LIOMixedState<S> step(RandomGenerator randomGenerator, ProbabilityMatrix<Agent> matrix) {
        return new LIOMixedState<>(definition, matrix.sample(randomGenerator, thisAgent), otherAgents.step(randomGenerator, matrix));
    }

    @Override
    public ProbabilityVector<LIOMixedState<S>> next(ProbabilityMatrix<Agent> matrix) {
        ProbabilityVector<Agent> agentNext = matrix.getRowOf(thisAgent);
        ProbabilityVector<S> otherNext = otherAgents.next(matrix);
        ProbabilityVector<LIOMixedState<S>> next = new ProbabilityVector<>();
        agentNext.iterate((a,p1) -> otherNext.iterate((o,p2) -> next.add(new LIOMixedState<>(definition, a, o), p1*p2)));
        return next;
    }

    @Override
    public ProbabilityVector<LIOMixedState<S>> next() {
        return next(definition.getAgentProbabilityMatrix(this));
    }
}
