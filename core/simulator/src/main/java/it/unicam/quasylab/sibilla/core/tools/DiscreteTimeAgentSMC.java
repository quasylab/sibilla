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

package it.unicam.quasylab.sibilla.core.tools;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.DiscreteTimeSimulationStepFunction;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.function.DoublePredicate;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiscreteTimeAgentSMC<S extends ImmutableState & IndexedState<A>,A> {

    private final DiscreteTimeSimulationStepFunction<S> stepFunction;
    private final RandomGenerator rg;
    private final IntFunction<S[]> arrayBuilder;

    public DiscreteTimeAgentSMC(DiscreteTimeSimulationStepFunction<S> stepFunction, IntFunction<S[]> arrayBuilder) {
        this(new DefaultRandomGenerator(), stepFunction, arrayBuilder);
    }

    public DiscreteTimeAgentSMC(RandomGenerator rg, DiscreteTimeSimulationStepFunction<S> stepFunction, IntFunction<S[]> arrayBuilder) {
        this.stepFunction = stepFunction;
        this.rg = rg;
        this.arrayBuilder = arrayBuilder;
    }

    public double[] compute( S initialState, DiscreteTimePathChecker<S,Boolean> f, int steps, double alpha, double eps) {
        double n = (1/(2*Math.pow(eps,2)))*Math.log(2/alpha);
        return compute(initialState, f, steps, n);
    }

    public double[] compute(S initialState, DiscreteTimePathChecker<S, Boolean> f, int steps, double n) {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        SimulationEnvironment se = new SimulationEnvironment();
        double[] result = new double[steps];
        for(int i=0; i<n;i++) {
            Trajectory<S> trj = se.sampleTrajectory(rg, stepFunction,initialState,steps);
            Boolean[] sat = f.eval(extractValues(trj,steps+1));
            IntStream.range(0,steps).forEach(k -> {
                if (sat[k]) {
                    result[k]++;
                }
            });
        }
        for(int i=0 ; i<result.length ; i++) {
            result[i]=result[i]/n;
        }
        return result;
    }

    private S[] extractValues(Trajectory<S> trj, int steps) {
        S[] res = arrayBuilder.apply(steps);
        S last = null;
        int counter = 0;
        for (Sample<S> sample: trj.getData()) {
            last = sample.getValue();
            res[counter++] = last;
        }
        for(int i=counter;i<steps;i++) {
            res[i]=last;
        }
        return res;
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getAtomic(Predicate<S> predicate) {
        return (p,l) -> IntStream.range(0,l).boxed().map(p::apply)
                .map(predicate::test).toArray(Boolean[]::new);
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getConjunction(DiscreteTimePathChecker<S,Boolean> f1, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> {
            Boolean[] v1 = f1.eval(p,l);
            Boolean[] v2 = f2.eval(p,l);
            return IntStream.range(0,l).boxed().map(i -> v1[i]&&v2[i]).toArray(Boolean[]::new);
        };
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getDisjunction(DiscreteTimePathChecker<S,Boolean> f1, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> {
            Boolean[] v1 = f1.eval(p,l);
            Boolean[] v2 = f2.eval(p,l);
            return IntStream.range(0,l).boxed().map(i -> v1[i]||v2[i]).toArray(Boolean[]::new);
        };
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getImply(DiscreteTimePathChecker<S,Boolean> f1, DiscreteTimePathChecker<S,Boolean> f2) {
        return getDisjunction(getNegation(f1),f2);
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getNegation(DiscreteTimePathChecker<S,Boolean> f) {
        return (p,l) -> {
            Boolean[] v = f.eval(p,l);
            return IntStream.range(0,l).boxed().map(i -> !v[i]).toArray(Boolean[]::new);
        };
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getUntil(DiscreteTimePathChecker<S,Boolean> f1, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> computeUnboundedUntil( f1.eval(p,l) , f2.eval(p,l));
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getUntil(DiscreteTimePathChecker<S,Boolean> f1, int k, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> computeBoundedUntil( f1.eval(p,l) , k, f2.eval(p,l));
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getNext(DiscreteTimePathChecker<S, Boolean> f) {
        return (p,l) -> {
            Boolean[] v = f.eval(p,l);
            return IntStream.range(0,v.length).boxed().map(i -> (i<v.length-1?v[i+1]:false)).toArray(Boolean[]::new);
        };
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getEventually(int k, DiscreteTimePathChecker<S,Boolean> f) {
        return (p,l) -> computeEventually(k, f.eval(p,l));
    }
    public static <S> DiscreteTimePathChecker<S,Boolean> getEventually(DiscreteTimePathChecker<S,Boolean> f) {
        return (p,l) -> computeEventually(f.eval(p,l));
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getGlobally(int k, DiscreteTimePathChecker<S,Boolean> f) {
        return (p,l) -> computeGlobally(k, f.eval(p,l));
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getGlobally(DiscreteTimePathChecker<S,Boolean> f) {
        return (p,l) -> computeGlobally(f.eval(p,l));
    }

    public static <S extends State & IndexedState<A>,A> DiscreteTimePathChecker<S,Boolean> getFractionOf(int size, DiscreteTimePathChecker<A,Boolean> f, DoublePredicate pred ) {
        return (p,l) -> {
            ArrayList<Boolean[]> values = IntStream.range(0,size).boxed().map(i -> f.eval(t -> p.apply(t).get(i),l)).collect(Collectors.toCollection(ArrayList::new));
            return IntStream.range(0,l).boxed().map(t -> pred.test( count(size,values,t) /size )).toArray(Boolean[]::new);
        };
    }

    public static double count(int size, ArrayList<Boolean[]> values, int t) {
        return IntStream.range(0,size).filter(i -> values.get(i)[t]).count();
    }

    public static Boolean[] computeUnboundedUntil(Boolean[] v1, Boolean[] v2) {
        int length = Math.max(v1.length,v2.length);
        Boolean[] res = new Boolean[length];
        boolean current = false;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v2[idx] || (current && v1[idx]);
            current = res[idx];
        }
        return res;
    }

    public static Boolean[] computeBoundedUntil(Boolean[] v1, int k, Boolean[] v2) {
        int length = Math.max(v1.length,v2.length);
        Boolean[] res = new Boolean[length];
        int counter = Integer.MAX_VALUE;
        boolean current = false;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v2[idx] || (current && (counter<k) && v1[idx]);
            current = res[idx];
            if (v2[idx]) {
                counter = 1;
            } else {
                counter = (counter == Integer.MAX_VALUE?Integer.MAX_VALUE:counter+1);
            }
        }
        return res;
    }

    private static Boolean[] computeEventually(int k, Boolean[] v) {
        Boolean[] res = new Boolean[v.length];
        int counter = Integer.MAX_VALUE;
        boolean current = false;
        int length = v.length;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v[idx] || (current && (counter<k));
            current = res[idx];
            if (v[idx]) {
                counter = 1;
            } else {
                counter = (counter == Integer.MAX_VALUE?Integer.MAX_VALUE:counter+1);
            }
        }
        return res;
    }

    private static Boolean[] computeGlobally(int k, Boolean[] v) {
        Boolean[] res = new Boolean[v.length];
        int counter = 0;
        int length = v.length;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v[idx] && (counter>=k);
            if (v[idx]) {
                counter++;
            } else {
                counter = 0;
            }
        }
        return res;
    }


    private static Boolean[] computeEventually(Boolean[] v) {
        Boolean[] res = new Boolean[v.length];
        boolean current = false;
        int length = v.length;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v[idx] || current;
            current = res[idx];
        }
        return res;
    }

    private static Boolean[] computeGlobally(Boolean[] v) {
        Boolean[] res = new Boolean[v.length];
        boolean current = true;
        int length = v.length;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v[idx] && current;
            current = res[idx];
        }
        return res;
    }

}
