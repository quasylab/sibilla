/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.tools.glotl.mc;

import it.unicam.quasylab.sibilla.core.models.lio.LIOCountingState;

import java.util.function.DoublePredicate;

public class GLoTLbModelCheckerPathProbability  extends GLoTLbModelCheckerAbstract {

    private final GLoTLGlobalPathEvaluator prop;

    private final DoublePredicate guard;

    public GLoTLbModelCheckerPathProbability(GLoTLGlobalPathEvaluator prop, DoublePredicate guard) {
        this.prop = prop;
        this.guard = guard;
    }

    @Override
    protected boolean compute(LIOCountingState state) {
        return guard.test(prop.eval(state));
    }

}
