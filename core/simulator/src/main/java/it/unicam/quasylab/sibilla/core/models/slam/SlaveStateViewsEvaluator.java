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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class is used to evaluate local and global views of a given state.
 */
public class SlaveStateViewsEvaluator {

    private final SlamStateViews views;
    private final SlamState state;

    private final Map<String, Boolean> cachedPredicate = new HashMap<>();

    private final Map<String, SlamValue> cachedGlobalViews = new HashMap<>();

    private final Map<SlamAgent, Map<String, Function<SlamState, SlamValue>>> cachedLocalFunctions = new HashMap<>();

    private final Map<SlamAgent, Map<String, SlamValue>> cachedLocalViews = new HashMap<>();

    /**
     * Creates a new evaluator that resolves the given views on the given state.
     *
     * @param views slam views.
     * @param state slam state on which views are computed.
     */
    public SlaveStateViewsEvaluator(SlamStateViews views, SlamState state) {
        this.views = views;
        this.state = state;
    }

    /**
     * Evaluates the predicate with the given name.
     *
     * @param name name of the predicate to evaluate.
     * @return true if the predicate is satisfied, false otherwise.
     */
    public boolean getPredicate(String name) {
        return cachedPredicate.computeIfAbsent(name, this::evalPredicate);
    }

    private Boolean evalPredicate(String name) {
        Predicate<SlamState> predicate  = views.getPredicate(name);
        return (predicate != null)&&(predicate.test(this.state));
    }

}
