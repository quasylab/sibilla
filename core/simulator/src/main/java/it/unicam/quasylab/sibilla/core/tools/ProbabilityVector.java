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

import it.unicam.quasylab.sibilla.core.models.IndexedState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * This class is used to represent a (sub) probability vector associating to
 * elements of type <code>S</code> a value between 0.0 and 1.0.
 *
 * @param <S> data types of indexed elements.
 */
public class ProbabilityVector<S> implements Iterable<ProbabilityEntries<S>> {


    public static <S> ProbabilityVector<S> getProbabilityVector(List<ProbabilityEntries<S>> entries) {
        ProbabilityVector<S> probabilityVector = new ProbabilityVector<>();
        entries.forEach(probabilityVector::add);
        return probabilityVector;
    }

    /**
     * Returns a sub-probability distribution associating v to element s.
     *
     * @param s elements with probability
     * @param v probability value
     * @return a sub-probability distribution associating v to element s
     *
     * @param <S> type of measured set
     *
     * @throws IllegalArgumentException if v is either less than 0 or grater than 1
     */
    public static <S> ProbabilityVector<S> of(S s, double v) {
        ProbabilityVector<S> vector = new ProbabilityVector<>();
        vector.add(s,v);
        return vector;
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
     * Returns the total probability mass of the element in this vector.
     *
     * @return the total probability mass of the element in this vector.
     */
    public double getTotalProbability() {
        return sum;
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

    /**
     * Returns the probability vector obtained by multiplying this object by
     * a scalar between 0 and 1.
     *
     * @param p a double value between 0 and 1.
     * @return the probability vector obtained by multiplying this object by
     * a scalar between 0 and 1.
     *
     * @throws IllegalArgumentException if p is greater than 1.0 or less than 0.0
     */
    public ProbabilityVector<S> scale(double p) {
        if ((p>1.0)||(p<0.0)) {
            throw new IllegalArgumentException("Probability must be a value between 0.0 and 1.0");
        }
        Map<S, Double> newProbabilityMap = this.elements.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()*p));
        double newSum = this.sum*p;
        return new ProbabilityVector<>(newProbabilityMap, newSum);
    }

    /**
     * Returns a probability vector that associates to each <code>s1</code>
     * different from the given <code>s</code> the probability <code>this.get(s1)</code>
     * and <code>(1-this.getTotalProbability))+this.get(s)</code> to <code>s</code>.
     *
     * @param s a state.
     * @return a new probability vector that associates to each <code>s1</code>
     * different from the given <code>s</code> the probability <code>this.get(s1)</code>
     *  and <code>(1-this.getTotalProbability))+this.get(s)</code> to <code>s</code>.
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

    /**
     * Returns the number of elements with positive probability in the vector.
     *
     * @return the number of elements with positive probability in the vector.
     */
    public int size() {
        return this.elements.size();
    }

    /**
     * Returns an element randomly selected in this vector with probability <code>this.getTotalProbability()</code>
     * or the given value <code>s</code> with probability <code>1-this.getTotalProbability()</code>.
     *
     * @param randomGenerator random generator used to sample the value
     * @param s a default value
     * @return an element randomly selected in this vector with probability <code>this.getTotalProbability()</code>
     * or the given value <code>s</code> with probability <code>1-this.getTotalProbability()</code>.
     */
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

    /**
     * Returns the summation of <code>function.applyAsDouble(s)*this.get(s)</code> for each element <code>s</code>
     * in this vector.
     *
     * @param function a function mapping elements of type <code>S</code> to double.
     * @return the summation of <code>function.applyAsDouble(s)*this.get(s)</code> for each element <code>s</code>
     * in this vector.
     */
    public double compute(ToDoubleFunction<S> function) {
        return this.elements.entrySet().stream().mapToDouble(e -> function.applyAsDouble(e.getKey())*e.getValue()).sum();
    }

    /**
     * Returns the application of given function to this vector. The given function <code>f</code> associates
     * each element of type <code>S</code> to probability vectors over elements of type <code>T</code>.
     * The result of the application is the sum of <code>f.apply(s).scale(this.get(s))</code>
     * for each <code>s</code> in this vector.
     *
     * @param f the function
     * @return the application of given function by this vector.
     * @param <T> the application of given function by this vector.
     */
    public <T> ProbabilityVector<T> apply(Function<S, ProbabilityVector<T>> f) {
        ProbabilityVector<T> result = new ProbabilityVector<>();
        for (Map.Entry<S, Double> e: this.elements.entrySet()) {
            result.add( f.apply(e.getKey()).scale(e.getValue()));
        }
        return result;
    }

    /**
     * Returns the application of given function to this vector. The resulting vector associates to each
     * valut <code>t</code> of type <code>T</code> the probability according to this vector of the set
     * of <code>s</code> such that <code>f.apply(s).equals(t)</code>.
     *
     * @param f a function from <code>S</code> to <code>T</code>>
     * @return the application of given function to this vector.
     * @param <T> the domain of the resulting probability vector.
     */
    public <T> ProbabilityVector<T> map(Function<S, T> f) {
        ProbabilityVector<T> result = new ProbabilityVector<>();
        this.elements.forEach((s,p) -> result.add(f.apply(s), p));
        return result;
    }

    /**
     * Returns the probability associated to the set of values satisfying the given predicate.
     *
     * @param pred a predicate
     * @return the probability associated to the set of values satisfying the given predicate.
     */
    public double get(Predicate<S> pred) {
        return this.elements.entrySet().stream().filter(e -> pred.test(e.getKey())).mapToDouble(Map.Entry::getValue).sum();
    }

    /**
     * Returns the (sub-)probability obtained from this vector by considering only the elements
     * satisfying the given predicate.
     *
     * @param pred a predicate
     * @return the (sub-)probability obtained from this vector by considering only the elements
     * satisfying the given predicate.
     */
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

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public Iterator<ProbabilityEntries<S>> iterator() {
        Iterator<Map.Entry<S, Double>> iterator = elements.entrySet().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ProbabilityEntries<S> next() {
                Map.Entry<S, Double> entry = iterator.next();
                return new ProbabilityEntries<>(entry.getKey(), entry.getValue());
            }
        };
    }

    @Override
    public void forEach(Consumer<? super ProbabilityEntries<S>> action) {
        elements.forEach((key, value) -> action.accept(new ProbabilityEntries<>(key, value)));
    }

    @Override
    public Spliterator<ProbabilityEntries<S>> spliterator() {
        Spliterator<Map.Entry<S, Double>> spliterator = elements.entrySet().stream().spliterator();
        return new ProbabilityEntrySplitIterator<>(spliterator);
    }

    private static class ProbabilityEntrySplitIterator<S> implements Spliterator<ProbabilityEntries<S>> {


        private final Spliterator<Map.Entry<S, Double>> splititerator;

        public ProbabilityEntrySplitIterator(Spliterator<Map.Entry<S, Double>> spliterator) {
            this.splititerator = spliterator;
        }

        @Override
        public boolean tryAdvance(Consumer<? super ProbabilityEntries<S>> action) {
            return splititerator.tryAdvance(entry -> action.accept(new ProbabilityEntries<>(entry.getKey(), entry.getValue())));
        }

        @Override
        public Spliterator<ProbabilityEntries<S>> trySplit() {
            Spliterator<Map.Entry<S, Double>> entrySpliterator = this.splititerator.trySplit();
            if (entrySpliterator == null) {
                return null;
            } else {
                return new ProbabilityEntrySplitIterator<>(entrySpliterator);
            }
        }

        @Override
        public long estimateSize() {
            return splititerator.estimateSize();
        }

        @Override
        public int characteristics() {
            return splititerator.characteristics();
        }
    }
}
