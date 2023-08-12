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

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Represents a state composed by a single agent operating in an environment approximated
 * in terms of mean-field equations.
 */
public class LIOMeanFieldState implements LIOState {

    private final LIOMeanFieldTrajectory trajectory;

    private final int time;

    public LIOMeanFieldState(LIOState state) {
        this(new LIOMeanFieldTrajectory(state), 0);
    }


    public LIOMeanFieldState(LIOMeanFieldTrajectory trajectory, int time) {
        this.trajectory = trajectory;
        this.time = time;
    }


    @Override
    public double fractionOf(LIOAgent a) {
        return trajectory.fractionOf(time, a);
    }

    @Override
    public double fractionOf(Predicate<LIOAgent> predicate) {
        return trajectory.fractionOf(time, predicate);
    }

    @Override
    public Set<LIOAgent> getAgents() {
        return null;
    }


    @Override
    public LIOMeanFieldState step(RandomGenerator randomGenerator, ProbabilityMatrix<LIOAgent> matrix) {
        return step();
    }

    private LIOMeanFieldState step() {
        return new LIOMeanFieldState(trajectory, time+1);
    }


    @Override
    public ProbabilityVector<LIOMeanFieldState> next(ProbabilityMatrix<LIOAgent> matrix) {
        return next();
    }

    @Override
    public ProbabilityVector<LIOMeanFieldState> next() {
        return ProbabilityVector.dirac(step());
    }

    @Override
    public LIOAgentDefinitions getAgentsDefinition() {
        return trajectory.getAgentsDefinition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LIOMeanFieldState that = (LIOMeanFieldState) o;
        return Objects.equals(this.trajectory, that.trajectory)&&(this.time==that.time);
    }

    @Override
    public int hashCode() {
        return time;
    }
}
