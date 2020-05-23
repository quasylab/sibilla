/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasilab.sibilla.core;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.TimeStep;
import quasylab.sibilla.core.past.State;

import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * An execution environment has the responsibility to interactively execute
 * a given model.
 */
public class ExecutionEnvironment<S extends State> {

    private RandomGenerator rg;
    private Model<S> model;
    private LinkedList<TimeStep<S>> history;
    private boolean completed = false;
    private double time;
    private S currentState;

    /**
     * Create a new ExecutionEnvironment that can be used to execute a given model
     * starting from a specific state.
     *
     * @param model model to execute.
     * @param init
     */
    public ExecutionEnvironment(RandomGenerator rg, Model<S> model, S init) {
        this.model = model;
        this.history = new LinkedList<>();
        this.time = 0.0;
        this.currentState = init;
        this.rg = rg;
    }

    /**
     * Returns current state in the running.
     *
     * @return current state in simulation run.
     */
    public S currentState() {
        return currentState;
    }

    /**
     * Performs a step in the run.
     *
     * @return false if current state is a deadlock state, true otherwise.
     */
    public boolean step() {
        TimeStep<S> step = model.next(rg,time,currentState);
        if (step != null) {
            this.history.add(new TimeStep<S>(step.getTime(),currentState));
            this.currentState = step.getValue();
            this.time += step.getTime();
            return true;
        } else {
            this.completed = true;
            return false;
        }
    }

    /**
     * Executes the simulation run until a give predicate on the current
     * state is satisfied or a deadlock state is reached.
     *
     * @param condition stopping predicate.
     * @return false if current state is a deadlock state, true otherwise.
     */
    public boolean step(Predicate<S> condition) {
        while (!condition.test(currentState)&&step()) {
        }
        return completed;
    }

    /**
     * Cancel the last step.
     *
     * @return true if there is a previous state, false otherwise.
     */
    public boolean previous() {
        if (!this.history.isEmpty()) {
            TimeStep<S> step = this.history.pollLast();
            this.currentState = step.getValue();
            this.time -= step.getTime();
        }
        return false;
    }

    /**
     * Restarts the session.
     *
     * @return true if the session can be restarted, false otherwise.
     */
    public boolean restart() {
        if (!this.history.isEmpty()) {
            TimeStep<S> step = this.history.peekFirst();
            this.currentState = step.getValue();
            this.time = 0.0;
            this.history = new LinkedList<>();
        }
        return false;
    }

    /**
     * Returns current simulation time.
     *
     * @return current simulation time.
     */
    public double currentTime() {
        return time;
    }

    /**
     * Returns the number of steps.
     * @return the number of steps.
     */
    public int steps() {
        return this.history.size();
    }
}
