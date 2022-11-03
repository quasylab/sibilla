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

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * This class is used to define a set of attributes.
 */
public class AttributeRegistry {


    private final String[] attributes;
    private final Map<String, Integer> attributeDictionary;

    /**
     * Creates attributes with the given names.
     *
     * @param attributes attribute names.
     */
    public AttributeRegistry(String ... attributes) {
        this.attributes = attributes;
        this.attributeDictionary = new HashMap<>();
        IntStream.range(0, attributes.length).forEach(i -> this.attributeDictionary.put(attributes[i], i));
    }

    /**
     * Allocate agent attributes with the given value.
     *
     * @param values attribute values.
     * @return agent attributes instantiated with the given values.
     */
    public AgentAttributes create(double ... values) {
        if (values.length != attributes.length) {
            throw new IllegalArgumentException("Wrong number of parameters. Expected "+attributes.length+" are "+values.length);
        }
        return new AgentAttributes(values);
    }


    public Predicate<AgentAttributes> getPredicate(String attribute, DoublePredicate predicate) {
        if (attributeDictionary.containsKey(attribute)) {
            int idx = attributeDictionary.get(attribute);
            return a -> predicate.test(a.get(idx));
        } else {
            return a -> false;
        }
    }


    public AttributeRandomExpression get(String attribute) {
        if (attributeDictionary.containsKey(attribute)) {
            int idx = attributeDictionary.get(attribute);
            return (r, attr) -> attr.get(idx);
        } else {
            return (r, attr) -> Double.NaN;
        }
    }

    public AttributesUpdateFunction set(String attribute, AttributeRandomExpression expr) {
        if (attributeDictionary.containsKey(attribute)) {
            int idx = attributeDictionary.get(attribute);
            AttributeUpdateFunctionClass updateFunction = new AttributeUpdateFunctionClass();
            updateFunction.set(idx, expr);
            return updateFunction;
        } else {
            return (r, attr) -> attr;
        }
    }
}
