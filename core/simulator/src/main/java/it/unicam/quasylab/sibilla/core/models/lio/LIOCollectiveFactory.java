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

package it.unicam.quasylab.sibilla.core.models.lio;

import java.util.function.Supplier;

/**
 * This class is used to instantiate a collective in its different representations.
 */
public class LIOCollectiveFactory {

    public LIOCollectiveFactory() {

    }


    public LIOIndividualState getIndividualState() {
        return null;
    }

    public LIOIndividualState getCountingState() {
        return null;
    }

    public LIOPopulationFraction getPopulationFractionState() {
        return null;
    }

    public LIOMixedState getMixedState(Agent agent) {
        return null;
    }

    public LIOMeanFieldState getMeanFieldState(Agent agent) {
        return null;
    }

}
