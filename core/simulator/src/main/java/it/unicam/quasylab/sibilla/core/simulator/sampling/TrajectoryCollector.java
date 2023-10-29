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

package it.unicam.quasylab.sibilla.core.simulator.sampling;

import it.unicam.quasylab.sibilla.core.simulator.Trajectory;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class TrajectoryCollector<S> implements Supplier<SamplingHandler<S>> {

    private final LinkedList<Trajectory<S>> trajectories;

    public TrajectoryCollector() {
        this.trajectories = new LinkedList<>();
    }

    private synchronized void recordTrajectory(Trajectory<S> trajectory) {
        this.trajectories.add(trajectory);
    }

    public List<Trajectory<S>> getTrajectories() {
        return  new LinkedList<>(trajectories);
    }

    public Trajectory<S> getTrajectory() {
        return trajectories.poll();
    }

    @Override
    public SamplingHandler<S> get() {
        return new SamplingHandler<S>() {

            private Trajectory<S> trajectory = null;

            @Override
            public void start() {
                if (trajectory != null) {
                    throw new IllegalStateException();//TODO: Add MEssage!
                }
                this.trajectory = new Trajectory<>();
            }

            @Override
            public void sample(double time, S state) {
                this.trajectory.add(time, state);
            }

            @Override
            public void end(double time) {
                recordTrajectory(this.trajectory);
            }
        };
    }
}
