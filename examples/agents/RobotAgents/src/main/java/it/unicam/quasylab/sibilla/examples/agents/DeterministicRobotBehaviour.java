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

package it.unicam.quasylab.sibilla.examples.agents;

import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentAction;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentBehaviour;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.VariableMapping;
import org.apache.commons.math3.random.RandomGenerator;

import static it.unicam.quasylab.sibilla.examples.agents.RoboticScenarioDefinition.*;

public class DeterministicRobotBehaviour implements AgentBehaviour {

    @Override
    public AgentAction step(RandomGenerator rg, double now, VariableMapping currentState, VariableMapping observations) {
        if (observations.get(GOAL_SENSOR)==0.0) {
            if (observations.get(FRONT_SENSOR) == 0) {
                return ChangeDirectionAction.UP;
            }
            if (observations.get(RIGHT_SENSOR) == 0) {
                return ChangeDirectionAction.RIGHT;
            }
            if (observations.get(LEFT_SENSOR) == 0) {
                return ChangeDirectionAction.LEFT;
            }
        } else {
            System.err.println("GOAL!!!!");
        }
        return ChangeDirectionAction.STAND;
    }
}
