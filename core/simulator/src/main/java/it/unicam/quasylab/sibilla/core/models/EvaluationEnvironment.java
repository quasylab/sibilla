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

package it.unicam.quasylab.sibilla.core.models;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * An EvaluationEnvironment is used to collect a set of parameters that are used in the generation of values
 * used in a simulation or analysis. These values typically are related to portion of specifications that depends
 * on used defined values.
 */
public class EvaluationEnvironment {


    private final CachedValues constants;
    private final Map<String, SibillaValue> attributes;
    private final Map<String, SibillaValue> values;
    private final PropertyChangeSupport changer;
    private boolean isChanged = false;



    public EvaluationEnvironment() {
        this(new CachedValues());
    }

    /**
     * Creates an empty EvaluationEnvironment.
     */
    public EvaluationEnvironment(CachedValues constants) {
        this.attributes = new TreeMap<>();
        this.values = new TreeMap<>();
        this.changer = new PropertyChangeSupport(this);
        this.constants = constants;
        this.constants.setEnvironment(this);
    }

    public EvaluationEnvironment(Map<String, SibillaValue> values, CachedValues constants) {
        this(constants);
        this.attributes.putAll(values);
        this.values.putAll(values);
    }

    /**
     * Get the value associate with a given name.
     *
     * @param name name of a parameter.
     * @return the value associate with a given name.
     */
    public synchronized SibillaValue get(String name) {
        return values.getOrDefault(name,SibillaValue.ERROR_VALUE);
    }

    /**
     * Associates a value to a given name.
     *
     * @param name name of a parameter.
     * @param value value to associate.q
     */
    public synchronized void set(String name, SibillaValue value) {
        if (!attributes.containsKey(name)) {
            throw new IllegalArgumentException("Parameter "+name+" is unknown.");
        }
        SibillaValue old = values.put(name,value);
        changer.firePropertyChange(name,old,value);
        this.isChanged = true;
    }

    /**
     * Register a parameter with a given default value.
     *
     * @param name name of a parameter.
     * @param value default value.
     */
    public synchronized void register(String name, SibillaValue value) {
        if (attributes.containsKey(name)) {
            throw new IllegalArgumentException("Parameter "+name+" is already defined in the environment.");
        }
        attributes.put(name,value);
        values.put(name,value);
        changer.firePropertyChange(name,null,value);
    }

    /**
     * Reset the given parameter to its default value.
     *
     * @param name name of a parameter.
     */
    public synchronized void reset(String name) {
        set(name,getDefault(name));
    }

    /**
     * Return the default value associated with the given parameter.
     * @param name parameter name.
     * @return the default value associated with the given parameter.
     */
    public synchronized SibillaValue getDefault(String name) {
        SibillaValue value = attributes.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Parameter "+name+" is unknown.");
        }
        return value;
    }

    /**
     * Reset all parameters to their default values.
     */
    public synchronized void reset( ) {
        for (Map.Entry<String, SibillaValue> e: attributes.entrySet()) {
            set(e.getKey(), e.getValue());
        }
    }

    /**
     * Return an array with all the registered parameters.
     *
     * @return the array with all the registered parameters.
     */
    public String[] getParameters() {
        return attributes.keySet().toArray(new String[0]);
    }


    /**
     * Add a PropertyChangeListener to the EvaluationEnvironment.
     *
     * @param listener a PropertyChangeListener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changer.addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener from the EvaluationEnvironment.
     *
     * @param listener a PropertyChangeListener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changer.removePropertyChangeListener(listener);
    }

    /**
     * Return the map associating each parameter with its current value.
     *
     * @return the map associating each parameter with its current value.
     */
    public Map<String, SibillaValue> getParameterMap() {
        return new TreeMap<>(values);
    }

    /**
     * Return a function used to resolve names.
     *
     * @return a function used to resolve names.
     */
    public Function<String, Double> getEvaluator() {
        if (isChanged) {
            constants.reset();
            constants.compute();
        }
        return s -> {
            double d = constants.get(s);
            if (Double.isNaN(d)) {
                return get(s).doubleOf();
            } else {
                return d;
            }
        };
    }

    public boolean isDefined(String name) {
        return attributes.containsKey(name)||constants.isDefined(name);
    }
}
