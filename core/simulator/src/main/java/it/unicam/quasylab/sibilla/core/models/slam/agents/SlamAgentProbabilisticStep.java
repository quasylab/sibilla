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

package it.unicam.quasylab.sibilla.core.models.slam.agents;

import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

/**
 * This functional interface is used to compute next configuration of an agent. This function takes
 * a random generator and an agent memory and returns an instance of AgentStepEffects.
 */
public class SlamAgentProbabilisticStep implements SlamAgentStep {

    private final List<StepCase> cases;

    public SlamAgentProbabilisticStep() {
        this.cases = new LinkedList<>();
    }

    @Override
    public Optional<SlamAgentStepEffect> apply(RandomGenerator rg, AgentStore m) {
        List<Pair<Double, SlamAgentStep>> enabledSteps = this.cases.stream().filter(c -> c.isEnabled(m)).map(c -> Pair.of(c.weight.applyAsDouble(rg, m), c.step)).collect(Collectors.toList());
        if (!enabledSteps.isEmpty()) {
            double selected = rg.nextDouble()*enabledSteps.stream().mapToDouble(Pair::getKey).sum();
            for (Pair<Double, SlamAgentStep> enabledStep : enabledSteps) {
                if (selected<enabledStep.getKey()) {
                    return enabledStep.getValue().apply(rg, m);
                } else {
                    selected -= enabledStep.getKey();
                }
            }
        }
        return Optional.empty();
    }

    public void add(BiFunction<RandomGenerator, AgentStore, SibillaValue> weight, SlamAgentStep step) {
        this.cases.add(new StepCase((rg, m) -> weight.apply(rg, m).doubleOf(), step));
    }

    public void add(BiFunction<RandomGenerator, AgentStore, SibillaValue> weight, Function<AgentStore, SibillaValue> guard, SlamAgentStep step) {
        this.cases.add(new StepCase(m -> guard.apply(m).booleanOf(), (rg, m) -> weight.apply(rg, m).doubleOf(), step));
    }


    private static class StepCase {

        private final Predicate<AgentStore> guard;

        private final ToDoubleBiFunction<RandomGenerator, AgentStore> weight;

        private final SlamAgentStep step;

        private StepCase(ToDoubleBiFunction<RandomGenerator, AgentStore> weight, SlamAgentStep step) {
            this(m -> true, weight, step);
        }

        private StepCase(SlamAgentStep step) {
            this(m -> true, (rg, m) -> 1.0, step);
        }

        private StepCase(Predicate<AgentStore> guard, ToDoubleBiFunction<RandomGenerator, AgentStore> weight, SlamAgentStep step) {
            this.guard = guard;
            this.weight = weight;
            this.step = step;
        }

        boolean isEnabled(AgentStore m) {
            return guard.test(m);
        }

        double stepWeight(RandomGenerator rg, AgentStore m) {
            return this.weight.applyAsDouble(rg, m);
        }


        Optional<SlamAgentStepEffect> apply(RandomGenerator rg, AgentStore m) {
            return this.step.apply(rg, m);
        }

    }
}
