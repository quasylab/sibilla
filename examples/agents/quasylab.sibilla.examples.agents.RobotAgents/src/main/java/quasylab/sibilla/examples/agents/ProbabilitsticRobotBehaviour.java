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

package quasylab.sibilla.examples.agents;

import org.apache.commons.math3.random.RandomGenerator;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentAction;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentBehaviour;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.VariableMapping;

import java.util.ArrayList;

import static quasylab.sibilla.examples.agents.RoboticScenarioDefinition.*;

public class ProbabilitsticRobotBehaviour implements AgentBehaviour {

	@Override
	public AgentAction step(RandomGenerator rg, double now, VariableMapping currentState,
			VariableMapping observations) {


		if (observations.get(GOAL_SENSOR) == 0.0) {
			ArrayList<AgentAction> actions = new ArrayList<>();
			if ((observations.get(FRONT_SENSOR) == 0)) {
				actions.add(ChangeDirectionAction.UP);
			} else {
				if (observations.get(BACK_SENSOR) == 0) {
					actions.add(ChangeDirectionAction.DOWN);
				}
			}
			if (observations.get(LEFT_SENSOR) == 0) {
				actions.add(ChangeDirectionAction.LEFT);
			}
			if (observations.get(RIGHT_SENSOR) == 0) {
				actions.add(ChangeDirectionAction.RIGHT);
			}
			if (!actions.isEmpty()) {
				return actions.get(rg.nextInt(actions.size()));
			}
		}
		return ChangeDirectionAction.STAND;
	}

}
