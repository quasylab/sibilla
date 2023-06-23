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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * This class is used to represent a (sub) probability vector associating to
 * elements of type <code>S</code> a value between 0.0 and 1.0.
 *
 * @param <S> data types of indexed elements.
 */
public class ProbabilityVector<S> {


    public static <S> ProbabilityVector<S> getProbabilityVector(List<ProbabilityEntries<S>> entries) {
        ProbabilityVector<S> probabilityVector = new ProbabilityVector<>();
        entries.forEach(probabilityVector::add);
        return probabilityVector;
    }

    private void add(ProbabilityEntries<S> e) {
        this.add(e.getElement(), e.getProbability());
    }

    private final Map<S, Double> elements;

    private double sum = 0.0;


    /**
     * Creates a probability vector containing the given elements and having the given
     * total probability.
     *
     * @param elements elements in the distribution.
     * @param sum total probability in the distribution.
     */
    private ProbabilityVector(Map<S, Double> elements, double sum) {
        this.elements = elements;
        this.sum = sum;
    }

    /**
     * Creates an empty probability vector associating each element in <code>S</code> with
     * probability <code>0.0</code>.
     */
    public <T> ProbabilityVector() {
        this(new HashMap<>(4096*2), 0.0);
    }

    public static <S> ProbabilityVector<S> dirac(S s) {
        return new ProbabilityVector<>(Map.of(s, 1.0), 1.0);
    }

    /**
     * Returns the probability associated with the given element.
     * @param element an element.
     * @return the probability associated with the given element.
     */
    public synchronized double getProbability(S element) {
        return elements.getOrDefault(element, 0.0);
    }

    /**
     * Increments the probability associated to the given element.
     *
     * @param element an element.
     * @param p a probability value.
     * @throws IllegalArgumentException when <code>getSum()+p>1.0</code> or when <code>(p<0)||(p>1.0)</code>.
     */
    public synchronized void add(S element, double p) {
        if ((p>1.0)||(p<0.0)) {
            throw new IllegalArgumentException("Probability must be a value between 0.0 and 1.0");
        }
        if (sum+p>1.0) {
            throw new IllegalArgumentException("The total probability mass in a vector cannot be greater than 1.0");
        }
        this.elements.put(element, p+ getProbability(element));
        this.sum += p;
    }

    /**
     * Performs the given action for each element considered in this probability until all entries
     * have been processed or the action throws an exception.
     */
    public void iterate(BiConsumer<S, Double> consumer) {
        this.elements.forEach(consumer);
    }

    /**
     * Returns the probability vector obtained from the combination of this probability vector with <code>other</code>
     * according to the operator <code>op</code>.
     *
     * @param op operator used to combine elements of the two vectors.
     * @param other another probability vector.
     * @return the probability vector obtained from the combination of this probability vector with <code>other</code>
     * according to the operator <code>op</code>.
     * @param <T> type of other
     * @param <R>
     */
    public <T,R> ProbabilityVector<R> apply(BiFunction<S,T,R> op, ProbabilityVector<T> other) {
        ProbabilityVector<R> result = new ProbabilityVector<>();
        this.elements.forEach((s,p1) -> other.elements.forEach( (t,p2) -> result.add(op.apply(s,t), p1*p2) ));
        return result;
    }

    public ProbabilityVector<S> scale(double p) {
        if ((p>1.0)||(p<0.0)) {
            throw new IllegalArgumentException("Probability must be a value between 0.0 and 1.0");
        }
        Map<S, Double> newProbabilityMap = this.elements.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()*p));
        double newSum = this.sum*p;
        return new ProbabilityVector<>(newProbabilityMap, newSum);
    }

    /**
     * Computes the vector measuring the probability to reach in one step other
     * states from <code>s</code>.
     *
     * @param s a state.
     * @return the vector measuring the probability to reach in one step other
     * states from <code>s</code>.
     */
    public ProbabilityVector<S> complete(S s) {
        if (sum >= 1.0) { return this; }
        Map<S, Double> newProbabilityMap = new HashMap<>(this.elements);
        double old = elements.getOrDefault(s, 0.0);
        newProbabilityMap.put(s, 1.0 - sum - old);
        return new ProbabilityVector<>(newProbabilityMap, 1.0);
    }


    /**
     * Sums this vector with the other.
     *
     * @param other the other probability vector to sum.
     */
    public synchronized void add(ProbabilityVector<S> other) {
        if (other.sum+this.sum > 1.0) {
            throw new IllegalArgumentException("The total probability mass in a vector cannot be greater than 1.0");
        }
        other.iterate(this::add);
    }

    public int size() {
        return this.elements.size();
    }

    public synchronized S sample(RandomGenerator randomGenerator, S s) {
        double d = randomGenerator.nextDouble();
        if (d<sum) {
            for(Map.Entry<S, Double> e: this.elements.entrySet()) {
                if (d<e.getValue()) {
                    return e.getKey();
                }
                d -= e.getValue();
            }
        }
        return s;
    }

    public double compute(ToDoubleFunction<S> function) {
        return this.elements.entrySet().stream().mapToDouble(e -> function.applyAsDouble(e.getKey())*e.getValue()).sum();
    }

    public <T> ProbabilityVector<T> apply(Function<S, ProbabilityVector<T>> f) {
        ProbabilityVector<T> result = new ProbabilityVector<>();
        for (Map.Entry<S, Double> e: this.elements.entrySet()) {
            result.add( f.apply(e.getKey()).scale(e.getValue()));
        }
        //this.elements.forEach( (s,p) -> result.add( f.apply(s).scale(p) ) );
        return result;
    }

    public <T> ProbabilityVector<T> map(Function<S, T> f) {
        ProbabilityVector<T> result = new ProbabilityVector<>();
        this.elements.forEach((s,p) -> result.add(f.apply(s), p));
        return result;
    }

    public double get(Predicate<S> pred) {
        return this.elements.entrySet().stream().filter(e -> pred.test(e.getKey())).mapToDouble(Map.Entry::getValue).sum();
    }

    public ProbabilityVector<S> filter(Predicate<S> pred) {
        ProbabilityVector<S> result = new ProbabilityVector<>();
        this.elements.forEach((s,p) -> {
            if (pred.test(s)) {
                result.add(s, p);
            }
        });
        return result;
    }

    @Override
    public String toString() {
        return elements + " <"+sum+">";
    }
}
