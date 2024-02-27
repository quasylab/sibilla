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

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.models.agents.VariableMapping;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The class <code>YodaSystem</code> represents
 * the enclosing system  available in the simulation
 * Each one has the following components:
 * <ul>
 *     <li>a global state containing the information of the available objects</li>
 *     <li>a list of available agents</li>
 *     <li>a GlobalStateUpdateFunction updating the global state</li>
 * </ul>
 */
public class YodaSystemState implements ImmutableState, IndexedState<YodaAgent> {


    private final YodaVariableMapping globalAttributes;

    private final List<YodaAgent> agents;
    private final List<YodaSceneElement> sceneElements;


    public YodaSystemState(List<YodaAgent> agents, List<YodaSceneElement> sceneElements) {
        this(null, agents, sceneElements);
    }

    public YodaSystemState(YodaVariableMapping globalAttributes, List<YodaAgent> agents, List<YodaSceneElement> sceneElements) {
        this.globalAttributes = globalAttributes;
        this.agents = agents;
        this.sceneElements = sceneElements;
    }


    /**
     * This method returns the entire list of available agents
     *
     * @return the entire list of available agents
     */
    public List<YodaAgent> getAgents() {
        return agents;
    }


    public YodaSystemState next(RandomGenerator rg) {
        List<YodaAgent> newAgents = this.agents.stream().parallel().map(a -> a.next(rg, this)).collect(Collectors.toList());
        return new YodaSystemState(newAgents, this.sceneElements);
    }


    @Override
    public YodaAgent get(int i) {
        return agents.get(i);
    }

    @Override
    public int numberOfAgents() {
        return agents.size();
    }


    private Stream<YodaSceneElement> getStreamOfElements() {
        return Stream.concat(this.agents.stream(), this.sceneElements.stream());
    }

    private Stream<YodaSceneElement> getStreamOfElements(YodaAgent agent) {
        return Stream.concat(this.agents.stream().filter(a -> a.getId() != agent.getId()), this.sceneElements.stream());
    }

    /**
     * Returns true if there exist an agent or an element satisfying the given predicate.
     *
     * @param p a predicate.
     * @return true if there exist an agent or an element satisfying the given predicate.
     */
    public boolean exists(Predicate<YodaSceneElement> p) {
        return getStreamOfElements().anyMatch(p);
    }

    /**
     * Returns true if there exists an element having a name in <code>elementNames</code> that satisfies the given predicate.
     *
     * @param elementNames set of element names.
     * @param p the predicate to evaluate.
     * @return true if there exists an element having a name in <code>agents</code> that satisfy the given predicate.
     */
    public boolean exists(Set<YodaElementName> elementNames, Predicate<YodaSceneElement> p) {
        return getStreamOfElements().filter(a -> elementNames.contains(a.getName())).anyMatch(p);
    }

    /**
     * Returns true if there exist an agent, different from <code>agent</code>, or an element satisfying the given predicate.
     *
     * @param agent an agent
     * @param p the agent predicate to evaluate
     * @return true if there exist an agent, different from <code>agent</code>, or an element satisfying the given predicate.
     */
    public boolean exists(YodaAgent agent, Predicate<YodaSceneElement> p) {
        return getStreamOfElements(agent).anyMatch(p);
    }

    /**
     * Returns true if there exist an element, different from <code>agent</code>, having its name in
     * <code>elementNames</code> that satisfies the given predicate.
     *
     * @param agent an agent
     * @param elementNames a set of names
     * @param p the predicate to evaluate
     * @return true if there exist an element, different from <code>agent</code>, having its name in
     * <code>elementNames</code> that satisfies the given predicate.
     */
    public boolean exists(YodaAgent agent, Set<YodaElementName> elementNames, Predicate<YodaSceneElement> p) {
        return getStreamOfElements(agent).filter(a -> elementNames.contains(a.getName())).anyMatch(p);
    }

    /**
     * Returns true if the agents and the element in the system satisfy the given predicate.
     *
     * @param p a predicate.
     * @return true  if the agents and the element in the system satisfy the given predicate.
     */
    public boolean forall(Predicate<YodaSceneElement> p) {
        return getStreamOfElements().allMatch(p);
    }


    /**
     * Returns true if all the elements having a name in <code>elementNames</code> satisfy the given predicate.
     *
     * @param elementNames set of element names.
     * @param p the predicate to evaluate.
     * @return true if there exists an element having a name in <code>agents</code> that satisfy the given predicate.
     */
    public boolean forall(Set<YodaElementName> elementNames, Predicate<YodaSceneElement> p) {
        return getStreamOfElements().filter(e -> elementNames.contains(e.getName())).allMatch(p);
    }

    /**
     * Returns true if all the elements, that are different from the given agent, satisfy the given predicate.
     *
     * @param agent an agent.
     * @param p the predicate to evaluate.
     * @return true if there exists an element having a name in <code>agents</code> that satisfy the given predicate.
     */
    public boolean forall(YodaAgent agent, Predicate<YodaSceneElement> p) {
        return getStreamOfElements(agent).allMatch(p);
    }

    /**
     * Returns true if all the elements, that are different from the given agent, that have a name in elementNames,
     * do satisfy the given predicate.
     *
     * @param agent an agent.
     * @param elementNames a set of element names
     * @param p the predicate to evaluate.
     * @return true if there exists an element having a name in <code>agents</code> that satisfy the given predicate.
     */
    public boolean forall(YodaAgent agent, Set<YodaElementName> elementNames, Predicate<YodaSceneElement> p) {
        return getStreamOfElements(agent).filter(e -> elementNames.contains(e.getName())).allMatch(p);
    }

    public SibillaValue min(ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue min(Set<YodaElementName> elementNames, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> elementNames.contains(a.getName())).mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue min(Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> elementNames.contains(a.getName())).filter(pred).mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue min(Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(pred).mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue min(YodaAgent agent, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue min(YodaAgent agent, Set<YodaElementName> elementNames, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(a -> elementNames.contains(a.getName())).mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue min(YodaAgent agent, Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(a -> elementNames.contains(a.getName())).filter(pred).mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue min(YodaAgent agent, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(pred).mapToDouble(f).min().orElse(Double.POSITIVE_INFINITY));
    }

    public SibillaValue max(ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue max(Set<YodaElementName> elementNames, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> elementNames.contains(a.getName())).mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue max(Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> elementNames.contains(a.getName())).filter(pred).mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue max(Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(pred).mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue max(YodaAgent agent, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue max(YodaAgent agent, Set<YodaElementName> elementNames, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(a -> elementNames.contains(a.getName())).mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue max(YodaAgent agent, Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(a -> elementNames.contains(a.getName())).filter(pred).mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue max(YodaAgent agent, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(pred).mapToDouble(f).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public SibillaValue mean(ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue mean(Set<YodaElementName> elementNames, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> elementNames.contains(a.getName())).mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue mean(Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> elementNames.contains(a.getName())).filter(pred).mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue mean(Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(pred).mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue mean(YodaAgent agent, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue mean(YodaAgent agent, Set<YodaElementName> elementNames, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(a -> elementNames.contains(a.getName())).mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue mean(YodaAgent agent, Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(a -> elementNames.contains(a.getName())).filter(pred).mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue mean(YodaAgent agent, Predicate<YodaSceneElement> pred, ToDoubleFunction<YodaSceneElement> f) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(pred).mapToDouble(f).average().orElse(0.0));
    }

    public SibillaValue count(YodaAgent agent, Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId())
                .filter(a -> elementNames.contains(a.getName())).filter(pred).count());
    }

    public SibillaValue count(YodaAgent agent, Predicate<YodaSceneElement> pred) {
        return SibillaValue.of(getStreamOfElements().filter(a -> a.getId() != agent.getId()).filter(pred).count());
    }

    public SibillaValue count(Set<YodaElementName> elementNames, Predicate<YodaSceneElement> pred) {
        return SibillaValue.of(getStreamOfElements()
                .filter(a -> elementNames.contains(a.getName())).filter(pred).count());
    }

    public SibillaValue count(Predicate<YodaSceneElement> pred) {
        return SibillaValue.of(getStreamOfElements().filter(pred).count());
    }
}
