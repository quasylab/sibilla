/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

package quasylab.sibilla.core.simulator;

/**
 * @author belenchia
 *
 */


import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public interface SimulationManager<S> {

    public void setSampling(SamplingFunction<S> sampling_function);
    /**
     * Same as runTasks(SamplingFunction<S> sampling_function) but computes reachability instead
     * @return List<Boolean> of tasks that reached predicate
     */
    public long reach();
    public void run(SimulationTask<S> task);
    public void waitTermination();
}
