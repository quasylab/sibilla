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


import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Instances of this class are used to represent the mean-field trajectories originated from a
 * given state.
 */
public class LIOMeanFieldTrajectory {

    private final AgentsDefinition agentsDefinition;
    private final ArrayList<LIOPopulationFraction> trajectory;
    private  LIOPopulationFraction lastStep;

    /**
     * Creates a new trajectory starting from the given state.
     *
     * @param state the initial state of the trajectory.
     */
    public LIOMeanFieldTrajectory(LIOState state) {
        this.agentsDefinition = state.getAgentsDefinition();
        this.trajectory = new ArrayList<>();
        this.lastStep = state.getPopulationFractionVector();
        this.trajectory.add(lastStep);
    }

    /**
     * Returns the fraction of agents in the given state at the given time.
     *
     * @param time time step.
     * @param a an agent state.
     * @return the fraction of agents in the given state at the given time.
     */
    public double fractionOf(int time, Agent a) {
        return get(time).fractionOf(a);
    }

    /**
     * Returns the fraction of agents satisfying the given predicate at the given time.
     *
     * @param time time step.
     * @param predicate an agent predicate.
     * @return the fraction of agents satisfying the given predicate at the given time.
     */
    public double fractionOf(int time, Predicate<Agent> predicate) {
        return get(time).fractionOf(predicate);
    }

    /**
     * Returns the agents definition occurring in this trajectory.
     *
     * @return the agents definition occurring in this trajectory.
     */
    public AgentsDefinition getAgentsDefinition() {
        return agentsDefinition;
    }

    /**
     * Returns the population fraction at the given step.
     *
     * @param step time step.
     * @return the population fraction at the given step.
     * @throws IllegalArgumentException when <code>step</code> is less than zero.
     */
    public synchronized LIOPopulationFraction get(int step) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        generateUpToStep(step);
        return trajectory.get(step);
    }

    /**
     * Generates the elements of this trajectory up to the given time step, if needed.
     *
     * @param step the last time step to generate.
     */
    private void generateUpToStep(int step) {
        while (trajectory.size()<=step) {
            lastStep = lastStep.multuply(agentsDefinition.getAgentProbabilityMatrix(lastStep));
            trajectory.add(lastStep);
        }
    }
}
