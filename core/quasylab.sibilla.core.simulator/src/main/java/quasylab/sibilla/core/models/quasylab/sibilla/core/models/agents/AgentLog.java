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

package quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class AgentLog {

    private final LinkedList<AgentStep> steps;

    public AgentLog() {
        this(new LinkedList<>());
    }

    public AgentLog(LinkedList<AgentStep> steps) {
        this.steps = steps;
    }

    public synchronized LinkedList<AgentStep> select(double[] state, double[] observations) {
        return steps.stream()
                    .filter(s -> s.sameConditions(state,observations))
                    .collect(Collectors.toCollection(LinkedList::new));
    }

    public synchronized void add(double[] state, double[] observations, AgentAction action) {
        steps.add(new AgentStep(state,observations,action));
    }
}
