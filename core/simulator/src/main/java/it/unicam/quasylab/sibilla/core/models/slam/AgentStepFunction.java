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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleBiFunction;

/**
 * This functional interface is used to compute next configuration of an agent. This function takes
 * a random generator and an agent memory and returns an instance of AgentStepEffects.
 */
@FunctionalInterface
public interface AgentStepFunction {

    AgentStepEffect apply(RandomGenerator rg, AgentMemory m);

    static AgentStepFunction step(AgentState state, AgentCommand command) {
        return (rg, m) -> new AgentStepEffect(state, command.execute(rg, m));
    }

    static AgentStepFunction select(ToDoubleBiFunction<RandomGenerator,AgentMemory>[] weights, AgentStepFunction[] functions) {
        return (rg, m) -> {
            AgentStepFunction selected = Util.select(rg, m, weights, functions);
            if (selected != null) {
                return selected.apply(rg,m);
            } else {
                return null;
            }
        };
    }
}
