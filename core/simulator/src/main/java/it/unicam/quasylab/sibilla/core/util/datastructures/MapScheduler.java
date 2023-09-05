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

public class MapScheduler<T extends Comparable<T>> implements Scheduler<T> {

    private final SibillaMap<Double, SibillaSet<T>> queue;

    public MapScheduler() {
        this(new SibillaMap<>());
    }

    private MapScheduler(SibillaMap<Double, SibillaSet<T>> queue) {
        this.queue = queue;
    }


    @Override
    public MapScheduler<T> schedule(T element, double time) {
        return new MapScheduler<>(this.queue.applyOrAddIfNotExists(time, set -> set.add(element), () -> SibillaSet.of(element)));
    }

    @Override
    public double getNextTime() {
        return queue.getMinKey().orElse(Double.NaN);
    }

    @Override
    public double getLastTime() {
        return queue.getMaxKey().orElse(Double.NaN);
    }

    @Override
    public Optional<Pair<ScheduledElements<T>, Scheduler<T>>> scheduleNext() {
        return this.queue.removeFirst().map(Pair.combine(ScheduledElements::new, MapScheduler::new));
    }

    @Override
    public Scheduler<T> unscheduled(double time, T activity) {
        return new MapScheduler<>(this.queue.apply(time, (SibillaSet<T>  s) -> s.remove(activity)));
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}


