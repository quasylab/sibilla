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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.SimulatorCursor;

import java.util.Optional;
import java.util.PriorityQueue;

public class SlamSimulatorCursor implements SimulatorCursor<AgentManager> {

    private final AgentManager currentState;
    private final double now;
    private final PriorityQueue<Activity> activities;

    public SlamSimulatorCursor(AgentManager init) {
        this.currentState = init;
        this.now = 0.0;
        this.activities = new PriorityQueue<>();
        fillQueue();
    }

    private void fillQueue() {
        currentState.stream().forEach(this::scheduleAgent);
    }

    private synchronized void scheduleAgent(Agent agent) {
        if (!Double.isNaN(agent.timeOfNextStep())) {
            activities.add( new Activity.AgentStepActivity(agent));
        }
    }


    @Override
    public boolean step() {
        return false;
    }

    @Override
    public AgentManager currentState() {
        return null;
    }

    @Override
    public double time() {
        return 0;
    }
}
