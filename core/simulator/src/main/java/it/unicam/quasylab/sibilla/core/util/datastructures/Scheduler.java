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

import java.util.Optional;

/**
 * A scheduler is used to schedulate elements over time.
 */
public interface Scheduler<T extends Comparable<T>> {

    /**
     * Returns the scheduler obtained from this one by adding an element that has to be executed at a given time.
     *
     * @param element the element to schedule
     * @param time the time at which the element must be scheduled
     * @return the scheduler obtained from this one by adding an element that has to be executed at a given time.
     */
    Scheduler<T> schedule(T element, double time);


    /**
     * Returns the time of next scheduled element.
     *
     * @return the time of next scheduled element.
     */
    double getNextTime();

    /**
     * Returns the time of the last scheduled element.
     *
     * @return the time of the last scheduled element.
     */
    double getLastTime();

    /**
     * Returns a pair consisting of the next scheduled event and the scheduler of the
     * next events.
     *
     * @return a pair consisting of the next scheduled event and the scheduler of
     * the next events
     */
    Optional<Pair<ScheduledElements<T>, Scheduler<T>>> scheduleNext();

    Scheduler<T> unscheduled(double time, T activity);

    boolean isEmpty();
}
