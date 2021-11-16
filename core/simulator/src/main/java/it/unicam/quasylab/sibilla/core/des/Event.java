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

package it.unicam.quasylab.sibilla.core.des;

import java.util.List;

/**
 * An event represents an activity scheduled at a given simulation time.
 *
 * @param <S>
 */
public interface Event<S> {

    /**
     * Return the unique integer identifying the event.
     *
     * @return the unique integer identifying the event.
     */
    int getEventId();

    /**
     * Return the time when the event is scheduled. Double.NaN is returned
     * if the event is waiting for a specific condition.
     *
     * @return the time when the event is scheduled. Double.NaN is returned
     *  if the event is waiting for a specific condition.
     */
    double getTime();

    /**
     * Set the time when the event is scheduled.
     *
     * @param time the time when the event is schedule.
     */
    void setTime(double time);

    /**
     * Return true if the event is enabled in the curent state.
     *
     * @param currentState current state.
     * @return true if the event is enabled.
     */
    boolean isEnabled(S currentState);

    /**
     * Execute the event in the current state and return its effects.
     *
     * @param currentState current state where the event is executed.
     * @return the effects resulting from event execution.
     */
    EventEffects<S> execute(S currentState);


}
