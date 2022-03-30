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
 * Identifies the effects of the execution of an event. These consists of a new state S,
 * a list of new events to schedule, a set of events to delete.
 *
 * @param <S> system state.
 */
public class EventEffects<S> {

    private final S nextState;

    private final List<Event<S>> scheduledEvents;

    private final List<Event<S>> cancelledEvents;


    /**
     * Create new effects resulting from the execution of an event.
     *
     * @param nextState next system state.
     * @param scheduledEvents news scheduled events.
     * @param cancelledEvents cancelled events.
     */
    public EventEffects(S nextState, List<Event<S>> scheduledEvents, List<Event<S>> cancelledEvents) {
        this.nextState = nextState;
        this.scheduledEvents = scheduledEvents;
        this.cancelledEvents = cancelledEvents;
    }

    /**
     * Return next state resulting from event execution.
     *
     * @return next state resulting from event execution.
     */
    public S getNextState() {
        return nextState;
    }

    /**
     * Return the list of events scheduled after event execution.
     *
     * @return the list of events scheduled after event execution.
     */
    public List<Event<S>> getScheduledEvents() {
        return scheduledEvents;
    }

    /**
     * Return the list of events that have been cancelled due to the
     * event execution.
     *
     * @return the list of events that have been cancelled due to the event execution.
     */
    public List<Event<S>> getCancelledEvents() {
        return cancelledEvents;
    }

}
