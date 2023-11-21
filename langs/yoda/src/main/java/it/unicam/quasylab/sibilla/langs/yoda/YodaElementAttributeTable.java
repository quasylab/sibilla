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

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaElementNameRegistry;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is used to store the info about declared attributes declared in a YODA element.
 */
public class YodaElementAttributeTable {

    private final Map<String, Set<String>> groups;

    private final Map<String, YodaElementAttribute> elementAttributes;

    private final Map<String, YodaType> attributeTypes;

    private Set<String> globalCommonAttributes;

    public YodaElementAttributeTable() {
        elementAttributes = new HashMap<>();
        groups = new HashMap<>();
        attributeTypes = new HashMap<>();
    }

    public YodaElementNameRegistry getRegistry() {
        YodaElementNameRegistry registry = new YodaElementNameRegistry();
        registry.create(elementAttributes.keySet());
        groups.forEach((gn, set) -> registry.addGroup(gn, set.stream().map(registry::get).collect(Collectors.toSet())));
        return registry;
    }

    public boolean isGroup(String name) {
        return groups.containsKey(name);
    }

    public void addGroup(String name, Set<String> elements) {
        groups.put(name, elements);
    }

    public void recordAgentAttributes(String name,
                                      Map<String, YodaType> environmentalAttributes,
                                      Map<String, YodaType> agentAttributes,
                                      Map<String, YodaType> observationAttributes) {
        if (checkTypes(environmentalAttributes)&&checkTypes(agentAttributes)&&checkTypes(observationAttributes)) {
            recordTypes(environmentalAttributes);
            recordTypes(agentAttributes);
            recordTypes(observationAttributes);
            HashSet<String> attributes = new HashSet<>(environmentalAttributes.keySet());
            attributes.addAll(agentAttributes.keySet());
            if (this.globalCommonAttributes == null) {
                this.globalCommonAttributes = new HashSet<>(attributes);
            } else {
                this.globalCommonAttributes.retainAll(attributes);
            }
            elementAttributes.put(name, new YodaElementAttribute(environmentalAttributes.keySet(), agentAttributes.keySet(), observationAttributes.keySet()));
        }
    }

    private void recordTypes(Map<String, YodaType> attributeTypes) {
        attributeTypes.forEach(this::recordType);
    }

    private void recordType(String name, YodaType type) {
        attributeTypes.put(name, type);
    }

    private boolean checkTypes(Map<String, YodaType> attributeTypes) {
        return attributeTypes.entrySet().stream().allMatch(e -> checkType(e.getKey(), e.getValue()));
    }

    private boolean checkType(String name, YodaType type) {
        YodaType existingType = attributeTypes.get(name);
        return (existingType == null)||YodaType.areCompatible(type, existingType);
    }

    public boolean isValidAgentAttribute(String name) {
        return this.elementAttributes.values().stream().allMatch(e -> e.isValidAgentAttribute(name));
    }

    public boolean isValidEnvironmentalAttribute(String name) {
        return this.elementAttributes.values().stream().allMatch(e -> e.isValidEnvironmentalAttribute(name));
    }

    public boolean isValidObservationsAttribute(String name) {
        return this.elementAttributes.values().stream().allMatch(e -> e.isValidObservationsAttribute(name));
    }

    public Map<String, YodaType> getSensingAttributesOf(String name) {
        if (isGroup(name)) {
            return getSensingAttributesOfGroup(name);
        } else {
            return getSensingAttributesOfAgent(name);
        }
    }

    public Predicate<String> getSensingAttributePredicateOf(String name) {
        Map<String, YodaType> map = getSensingAttributesOf(name);
        return map::containsKey;
    }

    private Map<String, YodaType> getSensingAttributesOfAgent(String name) {
        if (this.elementAttributes.containsKey(name)) {
            return getTypeOf(this.elementAttributes.get(name).getSensingAttributes());
        } else {
            return Map.of();
        }
    }

    private Map<String, YodaType> getSensingAttributesOfGroup(String name) {
        return getTypeOf(getSetOfEnvironmentAttributesOfGroup(name));
    }

    private Map<String, YodaType> getTypeOf(Set<String> attributes) {
        return  attributes.stream().collect(Collectors.toMap(s -> s, this.attributeTypes::get));
    }

    private Set<String> getSetOfEnvironmentAttributesOfGroup(String name) {
        return this.elementAttributes
                .values()
                .stream()
                .map(YodaElementAttribute::getSensingAttributes).flatMap(Set::stream).collect(Collectors.toSet());
    }

    public boolean isAttribute(String name) {
        return attributeTypes.containsKey(name);
    }

    public YodaType getTypeOf(String name) {
        return attributeTypes.getOrDefault(name, YodaType.NONE_TYPE);
    }

    public Predicate<String> getGroupExpressionValidAttributePredicate() {
        return s -> this.globalCommonAttributes.contains(s);
    }

    public boolean isGroupOrElement(String name) {
        return isGroup(name)||isElement(name);
    }

    public boolean isElement(String name) {
        return this.elementAttributes.containsKey(name);
    }

    public Predicate<String> getAccessibleAttributeOf(String agentName) {
        YodaElementAttribute attributes = this.elementAttributes.get(agentName);
        if (attributes == null) {
            return s -> false;
        } else {
            return s -> attributes.agentAttributes.contains(s)||attributes.observationsAttribute.contains(s);
        }
    }

    public boolean isAgentAttribute(String agentName, String s) {
        YodaElementAttribute elementAttribute = elementAttributes.get(agentName);
        return (elementAttribute != null)&&(elementAttribute.agentAttributes.contains(s));
    }


    public boolean isAgentObservation(String agentName, String attribute) {
        YodaElementAttribute elementAttribute = elementAttributes.get(agentName);
        return (elementAttribute != null)&&(elementAttribute.observationsAttribute.contains(attribute));
    }

    public Predicate<String> getSensingAttributeOf(String agentName) {
        YodaElementAttribute elementAttribute = elementAttributes.get(agentName);
        if (elementAttribute == null) {
            return s -> false;
        } else {
            return s -> elementAttribute.environmentalAttributes.contains(s)||elementAttribute.agentAttributes.contains(s);
        }
    }


    class YodaElementAttribute {

        private final Set<String> environmentalAttributes;

        private final Set<String> agentAttributes;

        private final Set<String> observationsAttribute;

        YodaElementAttribute(Set<String> environmentalAttributes, Set<String> agentAttributes, Set<String> observationsAttribute) {
            this.environmentalAttributes = environmentalAttributes;
            this.agentAttributes = agentAttributes;
            this.observationsAttribute = observationsAttribute;
        }

        public boolean isValidAgentAttribute(String name) {
            return !this.environmentalAttributes.contains(name)&&!this.observationsAttribute.contains(name);
        }

        public boolean isValidEnvironmentalAttribute(String name) {
            return !this.agentAttributes.contains(name)&&!this.observationsAttribute.contains(name);
        }

        public boolean isValidObservationsAttribute(String name) {
            return !this.agentAttributes.contains(name)&&!this.observationsAttribute.contains(name);
        }

        public Set<String> getSensingAttributes() {
            HashSet<String> sensingAttributes = new HashSet<>(this.environmentalAttributes);
            sensingAttributes.addAll(this.agentAttributes);
            return sensingAttributes;
        }

    }
}
