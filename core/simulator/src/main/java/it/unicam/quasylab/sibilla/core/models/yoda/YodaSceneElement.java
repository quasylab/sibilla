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

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Represents an element in the scene. This is a passive element (like an obstacles) that is equipped with
 * an immutable state.
 */
public class YodaSceneElement {

    private final int elementId;

    private final YodaElementName elementName;

    protected YodaVariableMapping environmentalAttributes;

    /**
     * Creates a new element with the given name, id and attributes.
     *
     * @param elementName element name.
     * @param attributes element attributes.
     */
    public YodaSceneElement(YodaElementName elementName, int elementId, YodaVariableMapping environmentalAttributes) {
        this.elementName = elementName;
        this.elementId = elementId;
        this.environmentalAttributes = environmentalAttributes;
    }

    /**
     * Returns the name of this element.
     *
     * @return the name of this element.
     */
    public YodaElementName getName() {
        return elementName;
    }

    /**
     * Returns the id of this element.
     *
     * @return the id of this element.
     */
    public int getId() {
        return elementId;
    }

    /**
     * Returns the value of the given attribute at this element.
     *
     * @param name attribute name
     * @return the value of the given attribute at this element.
     */
    public SibillaValue getEnvironmentalAttributeValue(YodaVariable name) {
        return environmentalAttributes.getValue(name);
    }

    /**
     * Checks if the given predicate is satisfied when evaluated on the environment attributes of this element.
     *
     * @param p the predicate to test
     * @return true if the given predicate is satisfied when evaluated on the attributes of this element.
     */
    public boolean test(Predicate<YodaVariableMapping> p) {
        return p.test(environmentalAttributes);
    }

    /**
     * Returns the application of the given function to the information associated with this agent.
     *
     * @param f the function to apply.
     * @return the application of the given function to the information associated with this agent.
     * @param <T> type returned by the given function.
     */
    public <T> T eval(Function<YodaVariableMapping, T> f) {
        return f.apply(environmentalAttributes);
    }

    public double eval(ToDoubleFunction<YodaVariableMapping> f) {
        return f.applyAsDouble(environmentalAttributes);
    }

    public SibillaValue get(YodaVariable var) {
        return getEnvironmentalAttributeValue(var);
    }
}
