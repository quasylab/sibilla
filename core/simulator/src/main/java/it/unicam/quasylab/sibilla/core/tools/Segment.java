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

/**
 * Identifies a segment of a piecewise constant signal.
 *
 * @param <T> data type of stored signal.
 */
public class Segment<T> {

    private final double start;
    private final double end;
    private final T value;

    /**
     * Create a new signal in the interval [start,end) with the given value.
     *
     * @param start segment start.
     * @param end segment end.
     * @param value segment value.
     */
    public Segment(double start, double end, T value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    /**
     * Return the beginning of the segment.
     *
     * @return the beginning of the segment.
     */
    public double getStart() {
        return start;
    }

    /**
     * Return the end of the signal.
     *
     * @return the end of the signal.
     */
    public double getEnd() {
        return end;
    }

    /**
     * Return the value in the signal.
     *
     * @return the value in the signal.
     */
    public T getValue() {
        return value;
    }


}
