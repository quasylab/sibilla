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

package it.unicam.quasylab.sibilla.langs.lio;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AgentRegistry {

    private int agentCounter = 0;

    private final Set<AgentElement> agents = new HashSet<>();

    public AgentRegistry() {
    }

    public void addAgents(String name, Range ... args) {
        if (args.length == 0) {
            this.addAgent(name);
        } else {
            List<List<Integer>> values = generateAllValues(args);
            for (List<Integer> agentArgs: values) {
                this.addAgent(name, agentArgs);
            }
        }
    }

    private List<List<Integer>> generateAllValues(Range[] args) {
        List<List<Integer>> values = List.of(new LinkedList<>());
        for (Range r: args) {
            int[] rValues = r.getValues();
            values = values.stream().flatMap(l -> IntStream.of(rValues).mapToObj(i -> {
                    List<Integer> newList = new LinkedList<>(l);
                    l.add(i);
                    return newList;
                }
            )).collect(Collectors.toList());
        }
        return values;
    }

    private void addAgent(String name, int ... args) {
        this.agents.add(new AgentElement(name, args));
    }

    private void addAgent(String name, List<Integer> args) {
        addAgent(name, args.stream().mapToInt(Integer::intValue).toArray());
    }


    private class AgentElement {

        private final int id;

        private final String name;

        private final int[] args;


        private AgentElement(String name, int[] args) {
            this.name = name;
            this.args = args;
            this.id = agentCounter++;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AgentElement that = (AgentElement) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        public boolean test(String name, Predicate<int[]> argPredicate) {
            return this.name.equals(name)&&argPredicate.test(args);
        }

        public int getID() {
            return this.id;
        }
    }

    public Set<Integer> select(String name, Predicate<int[]> argPredicate) {
        return this.agents.stream().filter(a -> a.test(name, argPredicate)).map(AgentElement::getID).collect(Collectors.toSet());
    }

    public int getIndexOf(String name, int[] args) {
        return getIndexOf(name, args2 -> Arrays.equals(args, args2));
    }

    public int getIndexOf(String name, Predicate<int[]> argPredicate) {
        Optional<AgentElement> o = agents.stream().filter(a -> a.test(name, argPredicate)).findFirst();
        return o.map(AgentElement::getID).orElseGet(() -> -1);
    }

}
