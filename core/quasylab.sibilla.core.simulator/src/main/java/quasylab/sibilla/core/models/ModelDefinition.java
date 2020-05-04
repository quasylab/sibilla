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

import quasylab.sibilla.core.past.State;

/**
 * This interface implements a factory that can be used to build a model that according
 * to some parameters.
 *
 * @param <S>
 */
public interface ModelDefinition<S extends State> {

    /**
     * Returns the number of parameters needed to build a state.
     *
     * @return the number of parameters needed to build a state.
     */
    int stateArity();

    /**
     * Returns the number of parameters needed to build a model.
     *
     * @return the number of parameters needed to build a model.
     */
    int modelArity();

    /**
     * Create the default state (that is the first one defined in the factory) with
     * the given parameters.
     *
     * @param parameters parameters to use in state creation.
     * @return the default state associated the given parameters.
     */
    S state(double ... parameters);

    /**
     * Creates a new {@ling Model} from a given set of parameters.
     *
     * @param args model arguments
     * @return a model built from a given set of parameters.
     */
    Model<S> createModel(double ... args);


}
