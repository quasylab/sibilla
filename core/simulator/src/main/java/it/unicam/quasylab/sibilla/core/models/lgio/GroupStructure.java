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

package it.unicam.quasylab.sibilla.core.models.lgio;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class GroupStructure {

    private final List<Set<Agent>> groups;


    public GroupStructure(List<Set<Agent>> groups) {
        this.groups = groups;
    }

    public ActionProbabilityFunction getProbabilityFunction(BiFunction<Set<Agent>, Agent, ActionProbabilityFunction> probability, Agent a) {
        LinkedList<Set<Agent>> activeGroups = groups.stream().filter(s -> s.contains(a)).collect(Collectors.toCollection(LinkedList::new));
        return computeProbability(activeGroups, probability, a);
    }

    private ActionProbabilityFunction computeProbability(LinkedList<Set<Agent>> activeGroups, BiFunction<Set<Agent>, Agent, ActionProbabilityFunction> probability, Agent a) {
        if (!activeGroups.isEmpty()) {
            Set<Agent> first = activeGroups.pollFirst();
            LinkedList<Set<Agent>> intersectionSet = activeGroups
                    .stream().map(s -> {
                        Set<Agent> intersection = new HashSet<>(s);
                        intersection.retainAll(first);
                        return intersection;
                    }).collect(Collectors.toCollection(LinkedList::new));
            return probability.apply(first, a)
                    .sum(computeProbability(activeGroups, probability, a))
                    .diff(computeProbability(intersectionSet, probability, a));
        } else {
            return new ActionProbabilityFunction();
        }
    }
}
