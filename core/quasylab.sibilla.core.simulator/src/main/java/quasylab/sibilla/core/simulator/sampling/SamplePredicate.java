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

package quasylab.sibilla.core.simulator.sampling;

import quasylab.sibilla.core.past.State;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * This functional interface is used to declare the stopping predicate of a simulation.
 *
 * @author loreti
 */
@FunctionalInterface
public interface SamplePredicate<S extends State> extends Serializable {

    public static <S extends State> SamplePredicate<S> statePredicate(Predicate<S> condition) {
        return (t, s) -> condition.test(s);
    }

    public static <S extends State> SamplePredicate<S> timeDeadlinePredicate(double d) {
        return (t, s) -> t >= d;
    }

    public boolean test(double time, S state);
}
