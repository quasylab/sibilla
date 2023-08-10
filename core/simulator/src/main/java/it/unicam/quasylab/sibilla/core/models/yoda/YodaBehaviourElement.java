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


import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.ToDoubleBiFunction;

public class YodaBehaviourElement {

    private final BiPredicate<YodaVariableMapping, YodaVariableMapping> guard;

    private final Map<YodaAction, ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping>> actions;

    public YodaBehaviourElement(BiPredicate<YodaVariableMapping, YodaVariableMapping> guard, Map<YodaAction, ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping>> actions) {
        this.guard = guard;
        this.actions = actions;
    }


    public boolean isEnabled(YodaVariableMapping agentState, YodaVariableMapping agentObservations) {
        return guard.test(agentState, agentObservations);
    }

    public WeightedStructure<YodaAction> eval(YodaVariableMapping agentState, YodaVariableMapping agentObservations) {
        WeightedStructure<YodaAction> enabledActions = new WeightedLinkedList<>();
        this.actions.forEach((action, f) -> enabledActions.add(f.applyAsDouble(agentState, agentObservations), action));
        return enabledActions;
    }

}
