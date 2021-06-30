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

package it.unicam.quasylab.sibilla.core.tools;

import java.util.LinkedList;
import java.util.List;

public final class Signal<T> {

    private final LinkedList<Segment<T>> segments;

    /**
     * Create a new empty signal.
     */
    public Signal() {
        this.segments = new LinkedList<>();
    }

    /**
     * Create a new signal with the given segments.
     *
     * @param segments signal segments
     */
    public Signal(List<Segment<T>> segments) {
        this.segments = new LinkedList<>(segments);
    }


    /**
     * Return the number of segments in the signal.
     *
     * @return the number of segments in the signal.
     */
    public int size() {
        return this.segments.size();
    }


    /**
     * Return the time at the beginning of the signal.
     *
     * @return the time at the beginning of the signal.
     */
    public double start() {
        return (segments.isEmpty()?Double.NaN:segments.getFirst().getStart());
    }


    /**
     * Return the time at the end of the signal.
     *
     * @return the time at the end of the signal.
     */
    public double end() {
        return (segments.isEmpty()?Double.NaN:segments.getLast().getEnd());
    }


    /**
     * Add new segment at the end of the signal. An IllegalArgumentException is thrown if parameter from
     * is not equal the result of method end().
     *
     * @param from beginning of new segment.
     * @param to end of new new segment.
     * @param value segment value.
     */
    public synchronized void add(double from, double to, T value) {
        double end = end();
        if (Double.isNaN(end)||(end == from)) {
            this.segments.add(new Segment<>(from,to,value));
        }
        throw new IllegalArgumentException("Discontinued segment! Expected starting at time "+end+" but is "+from);
    }


}
