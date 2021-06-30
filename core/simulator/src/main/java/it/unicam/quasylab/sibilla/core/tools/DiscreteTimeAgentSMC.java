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

import it.unicam.quasylab.sibilla.core.models.DiscreteModel;
import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.DoublePredicate;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DiscreteTimeAgentSMC<S extends State & IndexedState<A>,A> {

    private final DiscreteModel<S> model;
    private final RandomGenerator rg;
    private final IntFunction<S[]> arrayBuilder;

    public DiscreteTimeAgentSMC(DiscreteModel<S> model,IntFunction<S[]> arrayBuilder) {
        this(new DefaultRandomGenerator(), model, arrayBuilder);
    }

    public DiscreteTimeAgentSMC(RandomGenerator rg, DiscreteModel<S> model,IntFunction<S[]> arrayBuilder) {
        this.model = model;
        this.rg = rg;
        this.arrayBuilder = arrayBuilder;
    }

    public double[] compute( S initialState, DiscreteTimePathChecker<S,Boolean> f, int steps, double alpha, double eps) {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        SimulationEnvironment se = new SimulationEnvironment();
        double n = (1/(2*Math.pow(eps,2)))*Math.log(2/alpha);
        double[] result = new double[steps];
        for(int i=0; i<n;i++) {
            Trajectory<S> trj = se.sampleTrajectory(rg,model,initialState,steps);
            Boolean[] sat = f.eval(extractValues(trj,steps));
            IntStream.range(0,steps).forEach(k -> result[k]++);
        }
        return DoubleStream.of(result).parallel().map(d -> d/n).toArray();
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
        return (p,l) -> IntStream.range(0,l).boxed().map(i -> p.apply(i))
                .map(s -> predicate.test(s)).toArray(n -> new Boolean[n]);
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getConjunction(DiscreteTimePathChecker<S,Boolean> f1, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> {
            Boolean[] v1 = f1.eval(p,l);
            Boolean[] v2 = f2.eval(p,l);
            return IntStream.range(0,l).boxed().map(i -> v1[i]&&v2[i]).toArray(n -> new Boolean[n]);
        };
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getDisjunction(DiscreteTimePathChecker<S,Boolean> f1, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> {
            Boolean[] v1 = f1.eval(p,l);
            Boolean[] v2 = f2.eval(p,l);
            return IntStream.range(0,l).boxed().map(i -> v1[i]||v2[i]).toArray(n -> new Boolean[n]);
        };
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getNegation(DiscreteTimePathChecker<S,Boolean> f) {
        return (p,l) -> {
            Boolean[] v = f.eval(p,l);
            return IntStream.range(0,l).boxed().map(i -> !v[i]).toArray(n -> new Boolean[n]);
        };
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getUntil(DiscreteTimePathChecker<S,Boolean> f1, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> computeUnboundedUntil( f1.eval(p,l) , f2.eval(p,l));
    }

    public static <S> DiscreteTimePathChecker<S,Boolean> getUntil(DiscreteTimePathChecker<S,Boolean> f1, int k, DiscreteTimePathChecker<S,Boolean> f2) {
        return (p,l) -> computeBoundedUntil( f1.eval(p,l) , k, f2.eval(p,l));
    }

    public static <S extends State & IndexedState<A>,A> DiscreteTimePathChecker<S,Boolean> getFractionOf(int size, DiscreteTimePathChecker<A,Boolean> f, DoublePredicate pred ) {
        return (p,l) -> {
            Boolean[][] values = IntStream.range(0,size).boxed().map(i -> f.eval((t) -> (p.apply(t).get(i)),l)).toArray(i -> new Boolean[i][]);
            return IntStream.range(0,l).boxed().map(t -> pred.test( count(values,t) /size )).toArray(i -> new Boolean[i]);
        };
    }

    public static double count(Boolean[][] values, int t) {
        return IntStream.range(0,values.length).filter(i -> values[i][t]).count();
    }

    public static Boolean[] computeUnboundedUntil(Boolean[] v1, Boolean[] v2) {
        int length = Math.max(v1.length,v2.length);
        Boolean[] res = new Boolean[length];
        boolean current = false;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v2[idx] || (current && v1[idx]);
        }
        return res;
    }

    public static Boolean[] computeBoundedUntil(Boolean[] v1, int k, Boolean[] v2) {
        int length = Math.max(v1.length,v2.length);
        Boolean[] res = new Boolean[length];
        boolean current = false;
        int counter = Integer.MAX_VALUE;
        for(int i=1; i<=length; i++) {
            int idx = length-i;
            res[idx] = v2[idx] || (current && (counter<k) && v1[idx]);
            if (v2[idx]) {
                counter = 1;
            } else {
                counter = (counter == Integer.MAX_VALUE?Integer.MAX_VALUE:counter+1);
            }
        }
        return res;
    }
}
