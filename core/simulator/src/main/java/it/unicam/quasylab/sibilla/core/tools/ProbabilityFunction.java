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

package it.unicam.quasylab.sibilla.core.tools;

import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Instances of this class are used to associates elements of a measurable set <code>S</code>
 * with a probability value.
 *
 * @param <S> the type of measurable set
 */
public class ProbabilityFunction<S> implements ToDoubleFunction<S>, Iterable<ProbabilityEntries<S>> {

    private final Map<S, Double> elements;

    private ProbabilityFunction(Map<S, Double> elements) {
        this.elements = elements;
    }

    /**
     * Creates an empty measure associating each element with 0.
     */
    public ProbabilityFunction() {
        this(new HashMap<>(4096*2));
    }

    /**
     * Returns a probability function that associates to s the value p and 0 to all the
     * other elements.
     *
     * @param s an element
     * @param p the probability value
     * @return a probability function that associates to s the value p and 0 to all the
     * other elements.
     *
     * @param <S> type of measured elements
     */
    public static <S> ProbabilityFunction<S> of(S s, double p) {
        if ((p<0)||(p>1)) {
            throw new IllegalArgumentException("A probability value must be between 0 and 1");
        }
        HashMap<S, Double> elements = new HashMap<>();
        elements.put(s, p);
        return new ProbabilityFunction<>(elements);
    }


    @Override
    public double applyAsDouble(S element) {
        return getProbability(element);
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
        double newp = getProbability(element)+p;
        if ((newp>1.0)||(newp<0.0)) {
            throw new IllegalArgumentException("Probability must be a value between 0.0 and 1.0");
        }
        this.elements.put(element, newp);
    }

    /**
     * Performs the given action for each element considered in this probability until all entries
     * have been processed or the action throws an exception.
     */
    public void iterate(BiConsumer<S, Double> consumer) {
        this.elements.forEach(consumer);
    }

    /**
     * Returns the probability function associating to each value <code>s</code> the value
     * <code>p*this.applyAsDouble(s)</code>.
     *
     * @param p a double value between 0 and 1.
     * @return the probability function associating to each value <code>s</code> the value
     * <code>p*this.applyAsDouble(s)</code>.
     *
     * @throws IllegalArgumentException if p is greater than 1.0 or less than 0.0
     */
    public ProbabilityFunction<S> scale(double p) {
        if ((p>1.0)||(p<0.0)) {
            throw new IllegalArgumentException("Probability must be a value between 0.0 and 1.0");
        }
        Map<S, Double> newProbabilityMap = this.elements.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()*p));
        return new ProbabilityFunction<>(newProbabilityMap);
    }


    /**
     * Sums this function into the other.
     *
     * @param other the other probability function to sum.
     */
    public synchronized void add(ProbabilityFunction<S> other) {
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
     * Returns the summation of <code>function.applyAsDouble(s)*this.get(s)</code> for each element <code>s</code>
     * in this function.
     *
     * @param function a function mapping elements of type <code>S</code> to double.
     * @return the summation of <code>function.applyAsDouble(s)*this.get(s)</code> for each element <code>s</code>
     * in this function.
     */
    public double compute(ToDoubleFunction<S> function) {
        return this.elements.entrySet().stream().mapToDouble(e -> function.applyAsDouble(e.getKey())*e.getValue()).sum();
    }

    /**
     * Returns the application of this function with the given one this vector.
     * The given function <code>f</code> associates each element of type <code>S</code>
     * to a probability function over elements of type <code>T</code>.
     * The result of the application is the sum of <code>f.apply(s).scale(this.get(s))</code>
     * for each <code>s</code> in this vector.
     *
     * @param f the function
     * @return the application of given function by this vector.
     * @param <T> the application of given function by this object.
     */
    public <T> ProbabilityFunction<T> apply(Function<S, ProbabilityFunction<T>> f) {
        ProbabilityFunction<T> result = new ProbabilityFunction<>();
        for (Map.Entry<S, Double> e: this.elements.entrySet()) {
            result.add( f.apply(e.getKey()).scale(e.getValue()));
        }
        return result;
    }

    /**
     * Returns the application of given function to this object. The resulting function associates to each
     * value <code>t</code> of type <code>T</code> the total probability of the
     * set of <code>s</code> such that <code>f.apply(s).equals(t)</code>.
     *
     * @param f a function from <code>S</code> to <code>T</code>>
     * @return the application of given function to this object.
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
     * Returns the probability function defined only the elements
     * satisfying the given predicate.
     *
     * @param pred a predicate
     * @return the probability function defined only the elements
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
        return elements.toString();
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProbabilityFunction<?> that)) return false;
        return Objects.equals(elements, that.elements);
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

}
