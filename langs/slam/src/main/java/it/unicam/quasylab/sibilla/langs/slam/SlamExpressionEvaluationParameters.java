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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.StateExpressionEvaluator;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentVariable;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class SlamExpressionEvaluationParameters {

    private final RandomGenerator rg;

    private final AgentStore patternStore;

    private final AgentStore agentStore;

    private final StateExpressionEvaluator evaluator;

    private final Double dt;


    private SlamExpressionEvaluationParameters(RandomGenerator rg, AgentStore agentStore, AgentStore patternStore, StateExpressionEvaluator evaluator, Double dt) {
        this.rg = rg;
        this.patternStore = patternStore;
        this.agentStore = agentStore;
        this.evaluator = evaluator;
        this.dt = dt;
    }

    public SlamExpressionEvaluationParameters() {
        this(null, null, null, null, null);
    }

    public SlamExpressionEvaluationParameters(RandomGenerator rg, AgentStore agentStore) {
        this(rg, agentStore, null, null, null);
    }

    public SlamExpressionEvaluationParameters(AgentStore agentStore, AgentStore patternStore) {
        this(null, agentStore, patternStore, null, null);
    }

    public SlamExpressionEvaluationParameters(RandomGenerator rg, AgentStore store, double dt) {
        this(rg, store, null, null, dt);
    }

    public SlamExpressionEvaluationParameters(RandomGenerator rg, AgentStore store, StateExpressionEvaluator stateExpressionEvaluator) {
        this(rg, store, null, stateExpressionEvaluator, null);
    }

    public SlamExpressionEvaluationParameters(AgentStore store) {
        this(null, null, store, null, null);
    }

    public static SibillaValue evalScalarExpression(Function<SlamExpressionEvaluationParameters,SibillaValue> evaluationFunction) {
        return evaluationFunction.apply(new SlamExpressionEvaluationParameters());
    }

    public static SibillaValue evalAgentExpression(RandomGenerator rg, AgentStore store, Function<SlamExpressionEvaluationParameters,SibillaValue> evaluationFunction) {
        return evaluationFunction.apply(new SlamExpressionEvaluationParameters(rg, store));
    }



    public static boolean evalAgentPredicate(AgentStore thisAgentStore, AgentStore store, Function<SlamExpressionEvaluationParameters,SibillaValue> evaluationFunction) {
        return evaluationFunction.apply(new SlamExpressionEvaluationParameters(thisAgentStore, store)).booleanOf();
    }

    public static SibillaValue evalDtExpression(RandomGenerator rg, AgentStore m, double dt, Function<SlamExpressionEvaluationParameters, SibillaValue> dtExpression) {
        return dtExpression.apply(new SlamExpressionEvaluationParameters(rg, m, dt));
    }

    public static PerceptionFunctionExpression getPerceptionFunction(Function<SlamExpressionEvaluationParameters, SibillaValue> function) {
        return (RandomGenerator rg, StateExpressionEvaluator stateExpressionEvaluator, AgentStore store) -> function.apply(new SlamExpressionEvaluationParameters(rg, store, stateExpressionEvaluator));
    }

    public static SibillaValue evalAttribute(AgentStore store, Function<SlamExpressionEvaluationParameters, SibillaValue> function) {
        return function.apply(new SlamExpressionEvaluationParameters(store));
    }

    public static SibillaValue evalAgentExpression(AgentStore store, Function<SlamExpressionEvaluationParameters, SibillaValue> function) {
        return function.apply(new SlamExpressionEvaluationParameters(store));
    }

    public SibillaValue get(AgentVariable var) {
        if (agentStore != null) {
            return agentStore.getValue(var);
        }
        return SibillaValue.ERROR_VALUE;
    }

    public SibillaValue getFromPatternElement(AgentVariable var) {
        if (agentStore != null) {
            return agentStore.getValue(var);
        }
        return SibillaValue.ERROR_VALUE;
    }

    public SibillaValue sampleGaussian(SibillaValue mean, SibillaValue variance) {
        if (rg == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(mean.doubleOf()+rg.nextGaussian()*variance.doubleOf());
    }

    public SibillaValue sampleUniform(SibillaValue from, SibillaValue to) {
        if (rg == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(from.doubleOf()+rg.nextDouble()*(to.doubleOf()- from.doubleOf()));
    }

    public SibillaValue nextRandomValue() {
        if (rg == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(rg.nextDouble());
    }

    public SibillaValue getMinOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp, BiPredicate<AgentStore, SlamAgent> guard) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getMinOf(a -> evalAgentPatternExpression(patternStore, a, exp), a -> guard.test(agentStore, a)));
    }

    public SibillaValue getMinOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getMinOf(a -> evalAgentPatternExpression(agentStore, a, exp)));
    }

    public SibillaValue getMaxOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp, BiPredicate<AgentStore, SlamAgent> guard) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getMaxOf(a -> evalAgentPatternExpression(agentStore, a, exp), a -> guard.test(agentStore, a)));
    }

    public static double evalAgentPatternExpression(AgentStore agentStore, AgentStore patternStore, Function<SlamExpressionEvaluationParameters, SibillaValue> exp) {
        return exp.apply(new SlamExpressionEvaluationParameters(agentStore, patternStore)).doubleOf();
    }

    public SibillaValue getMaxOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getMaxOf(a -> evalAgentPatternExpression(agentStore, a, exp)));
    }

    public SibillaValue getSumOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp, BiPredicate<AgentStore, SlamAgent> guard) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getSumOf(a -> evalAgentPatternExpression(agentStore, a, exp), a -> guard.test(agentStore, a)));
    }

    public SibillaValue getSumOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getSumOf(a -> evalAgentPatternExpression(agentStore, a, exp)));
    }

    public SibillaValue getMeanOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp, BiPredicate<AgentStore, SlamAgent> guard) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getMeanOf(a -> evalAgentPatternExpression(agentStore, a, exp), a -> guard.test(agentStore, a)));
    }

    public SibillaValue getMeanOf(Function<SlamExpressionEvaluationParameters, SibillaValue> exp) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.getMeanOf(a -> evalAgentPatternExpression(agentStore, a, exp)));
    }


    public SibillaValue count(BiPredicate<AgentStore, SlamAgent> guard) {
        if (evaluator == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(evaluator.count(a -> guard.test(agentStore, a)));
    }

    public SibillaValue now() {
        if (agentStore == null) {
            return SibillaValue.ERROR_VALUE;
        }
        return SibillaValue.of(agentStore.now());
    }

    public SibillaValue dt() {
        if (dt != null) {
            return SibillaValue.of(dt);
        }
        return SibillaValue.ERROR_VALUE;
    }

    public SibillaValue forAll(BiPredicate<AgentStore, SlamAgent> predicate) {
        return SibillaValue.of(evaluator.forAll(a -> predicate.test(agentStore, a)));
    }

    public SibillaValue exists(BiPredicate<AgentStore, SlamAgent> predicate) {
        return SibillaValue.of(evaluator.exists(a -> predicate.test(agentStore, a)));
    }

}
