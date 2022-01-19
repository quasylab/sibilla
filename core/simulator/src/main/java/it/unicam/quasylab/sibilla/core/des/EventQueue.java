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

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class EventQueue<S> {

    private final static int INITIAL_CAPACITY = 100;

    private final PriorityQueue<Event<S>> scheduledEvents;
    private final List<Event<S>> pendingEvents;
    private double time = 0.0;

    public EventQueue() {
        this.scheduledEvents = new PriorityQueue<>(INITIAL_CAPACITY, new EventComparator<>());
        this.pendingEvents = new LinkedList<>();
    }




}
