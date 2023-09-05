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

package it.unicam.quasylab.sibilla.core.util.datastructures;

import java.util.Map;

/**
 * This class represents a scheduling event. It consists of a non-negative time value and of a
 * list of elements of type T.
 *
 * @param <T> type of scheduled elements.
 */
public class ScheduledElements<T extends Comparable<T>> {

    private final double time;

    private final SibillaSet<T> scheduledElements;

    /**
     * Creates a new event occurring at the given time and involving the given number list of elements.
     *
     * @param time the time at which the event has occurred
     * @param scheduledElements the list of elements involved in the event
     */
    public ScheduledElements(double time, SibillaSet<T> scheduledElements) {
        this.time = time;
        this.scheduledElements = scheduledElements;
    }

    public ScheduledElements(Map.Entry<Double, SibillaSet<T>> entry) {
        this(entry.getKey(), entry.getValue());
    }

    /**
     * Returns the time at which this event is occurred
     *
     * @return the time at which this event is occurred
     */
    public double getTime() {
        return time;
    }

    /**
     * Returns the list of elements occurred at the given time.
     *
     * @return the list of elements occurred at the given time.
     */
    public SibillaSet<T> getScheduledElements() {
        return scheduledElements;
    }


}
