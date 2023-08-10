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

package it.unicam.quasylab.sibilla.core.models.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.util.BehaviouralStepFunction;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleBiFunction;

/**
 * The interface <code>YodaBehaviour</code> represents
 * the behaviour allowing the agent to choose any available action
 */
@FunctionalInterface
public interface YodaBehaviour extends Serializable {

    /**
     * This method returns a distribution of possible actions
     *
     * @param currentInternalState the current internal state of the agent
     * @param observations the available observations of the agent
     * @return a distribution of possible actions
     */
    WeightedStructure<YodaAction> evaluate(YodaVariableMapping currentInternalState, YodaVariableMapping observations);

    /**
     * This method returns a single action from a distribution of actions
     *
     * @param rg a random generator
     * @param actionsDistribution a distribution of actions derived from the behaviour evaluation
     * @return a single action from a distribution of actions if actionsDistribution is more than zero or null if actionsDistribution is not more than zero
     */
    default YodaAction selectAction(RandomGenerator rg, WeightedStructure<YodaAction> actionsDistribution) {
        if (actionsDistribution.getTotalWeight()>0.0) {
            return actionsDistribution.select(actionsDistribution.getTotalWeight()*rg.nextDouble()).getElement();
        } else {
            return null;
        }
    }

    static YodaBehaviour behaviourOf(List<YodaBehaviourElement> elements , YodaBehaviourElement defaultBehaviour) {
        return ((currentInternalState, observations) -> {
            for (YodaBehaviourElement element: elements) {
                if (element.isEnabled(currentInternalState, observations)) {
                    return element.eval(currentInternalState, observations);
                }
            }
            return defaultBehaviour.eval(currentInternalState, observations);
        });
    }
}
