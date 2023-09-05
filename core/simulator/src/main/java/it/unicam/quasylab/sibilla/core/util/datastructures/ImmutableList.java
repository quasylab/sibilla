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

package it.unicam.quasylab.sibilla.core.util.datastructures;


import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * This interface represents an immutable list of objects of type T.
 *
 * @param <T> the type of elements stored in the list.
 */
public interface ImmutableList<T>  {

    static <T> ImmutableList<T> of(T element) {
        return new NonEmptyList<>(element, new EmptyList<>());
    }

    /**
     * Returns true if this list is empty, false otherwise.
     * @return true if this list is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the list obtained from this one by adding one element at the beginning.
     *
     * @param t the eleent to add in the list.
     * @return the list obtained from this one by adding one element at the beginning.
     */
    default ImmutableList<T> add(T t) {
        return new NonEmptyList<>(t, this);
    }

    /**
     * Returns the tail of this list.
     *
     * @return the tail of this list.
     */
    ImmutableList<T> tail();

    /**
     * Returns the first element in the list. A null value is returned if this list is emtpy.
     *
     * @return the first element in the list. A null value is returned if this list is emtpy.
     */
    T head();

    /**
     * Returns the number of elements in this list.
     * @return the number of elements in this list.
     */
    int length();

    /**
     * Returns the array of elements in this list.
     *
     * @param arrayAllocator function used to allocate the array in memory.
     * @return the array of elements in this list.
     */
    T[] toArray(IntFunction<T[]> arrayAllocator);


    /**
     * Returns the list obtained from this one by reverting the order of its elements.
     *
     * @return the list obtained from this one by reverting the order of its elements.
     */
    ImmutableList<T> reverse();

    /**
     * Returns the list obtained by applying the given function to
     * all the element of this list.
     *
     * @param function function to apply
     * @return the list obtained by applying the given function to all the element of this list.
     * @param <V> type of the elements in the resulting list
     */
    <V> ImmutableList<V> apply(Function<T,V> function);


    /**
     * Applies the given action to all the elements in this list.
     *
     * @param action the action to perform on the elements of this list.
     */
    void forEach(Consumer<T> action);


    /**
     * Returns the value obtained by applying the given function to all the elements of this,
     * starting from the end. If the list is empty, the initial value is returned, otherwise
     * l.redubuceFromLast(f, v) = f.apply(l.head(), tail.reduceFromLast(f, v)).
     *
     * @param function the function to apply
     * @param initial the initial value
     * @return the value obtained by applying the given function to all the elements of this,
     * starting from the end.
     * @param <V> type of the result
     */
    <V> V reduceFromLast(BiFunction<T,V,V> function, V initial);

    /**
     * Returns the value obtained by applying the given function to all the elements of this,
     * starting from the beginning. If the list is empty, the initial value is returned, otherwise
     * l.reduceFromFirst(f, v) = tail.reduceFromFirst(f, f.apply(l.head(), v)).
     *
     * @param function the function to apply
     * @param initial the initial value
     * @return the value obtained by applying the given function to all the elements of this,
     * starting from the end.
     * @param <V> type of the result
     */
    <V> V reduceFromFirst(BiFunction<T,V,V> function, V initial);

    static <T> ImmutableList<T> empty(Class<T> clazz) {
        return new EmptyList<>();
    }

    class EmptyList<T> implements ImmutableList<T> {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ImmutableList<T> tail() {
            return null;
        }

        @Override
        public T head() {
            return null;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public T[] toArray(IntFunction<T[]> arrayAllocator) {
            return arrayAllocator.apply(0);
        }

        @Override
        public ImmutableList<T> reverse() {
            return this;
        }

        @Override
        public <V> ImmutableList<V> apply(Function<T, V> function) {
            return new EmptyList<>();
        }

        @Override
        public void forEach(Consumer<T> action) {}

        @Override
        public <V> V reduceFromLast(BiFunction<T, V, V> function, V initial) {
            return initial;
        }

        @Override
        public <V> V reduceFromFirst(BiFunction<T, V, V> function, V initial) {
            return initial;
        }
    }

    class NonEmptyList<T> implements ImmutableList<T> {

        private final T head;
        private final ImmutableList<T> tail;

        private final int length;

        public NonEmptyList(T head, ImmutableList<T> tail) {
            this.head = head;
            this.tail = tail;
            this.length = tail.length()+1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ImmutableList<T> tail() {
            return tail;
        }

        @Override
        public T head() {
            return head;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public T[] toArray(IntFunction<T[]> arrayAllocator) {
            T[] toReturn = arrayAllocator.apply(this.length());
            ImmutableList<T> scan = this;
            for(int i=0; (i<toReturn.length)&&(!scan.isEmpty()); i++) {
                toReturn[i] = scan.head();
                scan = scan.tail();
            }
            return toReturn;
        }

        @Override
        public ImmutableList<T> reverse() {
            ImmutableList<T> result = new EmptyList<>();
            ImmutableList<T> scan = this;
            while (!scan.isEmpty()) {
                result = result.add(scan.head());
                scan = scan.tail();
            }
            return result;
        }

        @Override
        public <V> ImmutableList<V> apply(Function<T, V> function) {
            return new NonEmptyList<>(function.apply(this.head), this.tail.apply(function));
        }

        @Override
        public void forEach(Consumer<T> action) {
            action.accept(this.head);
            this.tail.forEach(action);
        }

        @Override
        public <V> V reduceFromLast(BiFunction<T, V, V> function, V initial) {
            return function.apply(this.head, this.tail.reduceFromLast(function, initial));
        }

        @Override
        public <V> V reduceFromFirst(BiFunction<T, V, V> function, V initial) {
            return this.tail.reduceFromLast(function, function.apply(this.head, initial));
        }
    }

}
