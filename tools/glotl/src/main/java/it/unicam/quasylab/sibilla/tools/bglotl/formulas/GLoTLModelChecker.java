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

package it.unicam.quasylab.sibilla.tools.bglotl.formulas;


import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityEntries;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.tools.bglotl.GLoTLModelCheckerEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class GLoTLModelChecker<T,S extends IndexedState<T>> {

    private final GLoTLModelCheckerEnvironment<T, S> environment;

    private Map<GLoTLStateFormula, Map<S, Boolean>> globalStateCache = new HashMap<>();
    private Map<GLoTLStatePathFormula, Map<S, Double>> globalStatePathCache = new HashMap<>();
    private Map<GLoTLLocalPathFormula, Map<Pair<T,S>, Double>> localPathCache = new HashMap<>();

    public GLoTLModelChecker(GLoTLModelCheckerEnvironment<T, S> environment) {
        this.environment = environment;
    }


    public boolean check(S state, GLoTLStateFormula formula) {
        Map<S, Boolean> cache = globalStateCache.computeIfAbsent(formula, k -> new HashMap<>());
        return cache.computeIfAbsent(state, s -> doCheck(s, formula));
    }

    private Boolean doCheck(S state, GLoTLStateFormula formula) {
        return switch (formula) {
            case GLoTLStateAtomicFormula gLoTLStateAtomicFormula ->
                    environment.getGlobalPredicate(gLoTLStateAtomicFormula.name()).test(state);
            case GLoTLStateConjunctionFormula gLoTLStateConjunctionFormula ->
                    check(state, gLoTLStateConjunctionFormula.leftArgument())
                            && check(state, gLoTLStateConjunctionFormula.rightArgument());
            case GLoTLStateDisjunctionFormula gLoTLStateDisjunctionFormula ->
                    check(state, gLoTLStateDisjunctionFormula.leftArgument())
                            || check(state, gLoTLStateDisjunctionFormula.rightArgument());
            case GLoTLStateExpectedFormula gLoTLStateExpectedFormula -> false;
            case GLoTLStateFalseFormula gLoTLStateFalseFormula -> false;
            case GLoTLStateImplicationFormula gLoTLStateImplicationFormula ->
                    (!check(state, gLoTLStateImplicationFormula.firstArgument()))
                    && check(state, gLoTLStateImplicationFormula.secondArgument());
            case GLoTLStateNegationFormula gLoTLStateNegationFormula ->
                    !check(state, gLoTLStateNegationFormula.argument());
            case GLoTLStateProbabilityFormula gLoTLStateProbabilityFormula ->
                    gLoTLStateProbabilityFormula.probabilityBound().test(computePathProbability(state, gLoTLStateProbabilityFormula.pathFormula()));
            case GLoTLStateTrueFormula gLoTLStateTrueFormula -> true;
        };
    }

    public double computePathProbability(S state, GLoTLStatePathFormula formula) {
        return globalStatePathCache
                .computeIfAbsent(formula, k -> new HashMap<>())
                .computeIfAbsent(state, s -> doComputePathProbability(s, formula));
    }


    public double doComputePathProbability(S state, GLoTLStatePathFormula formula) {
        return switch (formula) {
            case GLoTLStatePathEventuallyFormula gLoTLStatePathEventuallyFormula ->
                    GLoTLModelChecker.computeReachProbability(state,
                            s -> true,
                            s -> check(s , gLoTLStatePathEventuallyFormula.argument()),
                            gLoTLStatePathEventuallyFormula.from(),
                            gLoTLStatePathEventuallyFormula.to(),
                            environment::step
                    );
            case GLoTLStatePathGloballyFormula gLoTLStatePathGloballyFormula ->
                    1 - GLoTLModelChecker.computeReachProbability(state,
                            s -> true,
                            s -> check(s , gLoTLStatePathGloballyFormula.argument()),
                            gLoTLStatePathGloballyFormula.from(),
                            gLoTLStatePathGloballyFormula.to(),
                            environment::step
                    );
            case GLoTLStatePathNextFormula gLoTLStatePathNextFormula ->
                    GLoTLModelChecker.computeNextProbability(state, s -> check(s, gLoTLStatePathNextFormula.nextFormula()), environment::step);
            case GLoTLStatePathUntilFormula gLoTLStatePathUntilFormula ->
                    GLoTLModelChecker.computeReachProbability(state,
                            s -> true,
                            s -> check(s , gLoTLStatePathUntilFormula.rightArgument()),
                            gLoTLStatePathUntilFormula.from(),
                            gLoTLStatePathUntilFormula.to(),
                            environment::step
                    );
        };
    }

    public double computePathProbability(T agent, S context, GLoTLLocalPathFormula formula) {
        if (formula.isSuccessful()) {
            return 1.0;
        }
        if (formula.isUnsuccessful()) {
            return 0.0;
        }
        Pair<T, S> pair = new Pair<>(agent, context);
        Map<Pair<T,S>, Double> cache = localPathCache.computeIfAbsent(formula, k -> new HashMap<>());
        return cache.computeIfAbsent(pair, p -> doComputeLocalProbability(agent, context, formula));
    }

    private double doComputeLocalProbability(T agent, S context, GLoTLLocalPathFormula formula) {
        GLoTLLocalPathFormula nextFormula = formula.step(agent, environment::getLocalPredicate);
        ProbabilityVector<Pair<T, S>> step = environment.step(agent, context);
        double sum = 0.0;
        for (ProbabilityEntries<Pair<T, S>> pep : step) {
            sum += computePathProbability(pep.getElement().getKey(), pep.getElement().getValue(), nextFormula)*pep.getProbability();
        }
        return sum;
    }

    /**
     * Computes the probability to be at the different states after the given number of steps
     * starting from the given probability distribution while the given transient predicate is
     * satisfied.
     *
     * @param steps number of steps
     * @param initial initial probability distribution
     * @param transientPredicate predicate that must be satisfied by reachable states
     * @return the probability to be at the different states after the given number of steps
     * starting from the given probability distribution while the given transient predicate is
     * satisfied.
     */
    public static <S> ProbabilityVector<S> doSteps(int steps, ProbabilityVector<S> initial, Predicate<S> transientPredicate, Function<S, ProbabilityVector<S>> stepFunction) {
        ProbabilityVector<S> transientProbability = initial.filter(transientPredicate);
        for(int i = 0; (i<steps)&&(transientProbability.getTotalProbability()>0); i++) {
            transientProbability = transientProbability.apply(s -> stepFunction.apply(s).filter(transientPredicate));
        }
        return transientProbability;
    }

    /**
     * Returns the probability that starting from <code>state</code> a state that satisfies
     * <code>transientPredicate</code> can be reached in a number of
     * steps between <code>from</code> and <code>to</code> while only states satisfying
     * <code>reachPredicate</code> are traversed.
     *
     * @param state initial state
     * @param transientPredicate predicate that must be satisfied by transient states
     * @param reachPredicate predicate that must be satisfied by goal states
     * @param from minimum number of steps to reach goal states
     * @param to maximum number of steps needed to reach goal states
     * @param stepFunction function used to compute the one-step-probability.
     * @return the probability that starting from <code>state</code> a state that satisfies
     * <code>transientPredicate</code> can be reached in a number of
     * steps between <code>from</code> and <code>to</code> while only states satisfying
     * <code>reachPredicate</code> are traversed.
     * @param <S> states data type
     */
    public static <S> double computeReachProbability(S state, Predicate<S> transientPredicate, Predicate<S> reachPredicate, int from, int to, Function<S, ProbabilityVector<S>> stepFunction) {
        ProbabilityVector<S> reachProbability = doSteps(from-1, ProbabilityVector.of(state, 1.0), transientPredicate, stepFunction);
        int step = from-1;
        double reachProbabilitySum = 0;
        while ((step < to)&&(reachProbability.getTotalProbability()>0)) {
            reachProbability = reachProbability.apply(stepFunction);
            reachProbabilitySum += reachProbability.get(reachPredicate);
            reachProbability = reachProbability.filter(Predicate.not(reachPredicate)).filter(transientPredicate);
            step++;
        }
        return reachProbabilitySum;
    }


    /**
     * Returns the probability that <code>state</code> reaches in one step a state satisfying predicate <code>formula</code>-
     *
     * @param state a state
     * @param formula state predicate
     * @param stepFunction function used to compute the one-step-probability
     * @return the probability that <code>state</code> reaches in one step a state satisfying predicate <code>formula</code>-
     * @param <S> type of system states
     */
    public static <S>  double computeNextProbability(S state, Predicate<S> formula, Function<S, ProbabilityVector<S>> stepFunction) {
        return stepFunction.apply(state).filter(formula).getTotalProbability();
    }

}
