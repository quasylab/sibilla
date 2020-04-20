/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasylab.sibilla.core.models;

import quasylab.sibilla.core.util.SibillaMessages;

import java.util.Objects;

/**
 * Represents the result of a time step. It consists of a value <code>S</code>, that
 * is the one we have the end of the step, and a time length.
 *
 * @param <S>
 */
public class TimeStep<S> {

    /**
     * Lenght of time step.
     */
    private final double time;

    /**
     * Value at the end of time step.
     */
    private final S value;

    /**
     * Creates a new time step with a specified length and value.
     *
     * @param time the length of time step. This value must be greater than 0.
     * @param value the value at the end of time step. This must be a non null value.
     */
    public TimeStep(double time, S value) {
        Objects.nonNull(value);
        if (time<=0) {
            throw new IllegalArgumentException(SibillaMessages.createdTimeStepWithNonPositiveTime(time));
        }
        this.time = time;
        this.value = value;
    }

    /**
     * Returns the value at the end of time step.
     *
     * @return the value at the end of time step.
     */
    public S getValue() {
        return value;
    }

    /**
     * Returns the length of time step.
     *
     * @return the length of time step.
     */
    public double getTime() {
        return time;
    }
}
