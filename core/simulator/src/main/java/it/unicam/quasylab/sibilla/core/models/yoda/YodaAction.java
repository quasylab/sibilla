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

import it.unicam.quasylab.sibilla.core.util.values.SibillaRandomBiFunction;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The interface <code>YodaAction</code> represents
 * the action done by agents in a system.
 *
 */
public interface YodaAction extends Serializable, SibillaRandomBiFunction<YodaVariableMapping, YodaVariableMapping, YodaVariableMapping> {

    /**
     * This method returns the name of this action
     *
     * @return the name of this action
     */
    String getName();

    /**
     * This method returns the new internal state of an agent after performing an action
     *
     * @param rg a random generator
     * @param currentState the agent current internal state
     * @return the new internal state of an agent after performing an action
     */
    YodaVariableMapping eval(RandomGenerator rg, YodaVariableMapping agentState, YodaVariableMapping agentObservations);


    /**
     * Returns the action with the given name that transforms the state according to the given function.
     *
     * @param name action name
     * @param f action function
     * @return returns the action with the given name that transforms the state according to the given function.
     */
    static YodaAction getAction(String name, SibillaRandomBiFunction<YodaVariableMapping, YodaVariableMapping, YodaVariableMapping> f) {
        return new YodaAction() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public YodaVariableMapping eval(RandomGenerator rg, YodaVariableMapping agentState, YodaVariableMapping agentObservations) {
                return f.eval(rg, agentState, agentObservations);
            }
        };
    }


    static YodaAction actionOf(String name, SibillaRandomBiFunction<YodaVariableMapping,YodaVariableMapping, List<YodaVariableUpdate>> updateFunction ) {
        return getAction(name, (rg, state, observations) -> {
            YodaVariableMapping result = state;
            List<YodaVariableUpdate> updates = updateFunction.eval(rg, state, observations);
            for (YodaVariableUpdate u: updates) {
                result = result.setValue(u.getVariable(), u.getValue());
            }
            return result;
        });
    }



}
