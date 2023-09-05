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


import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Instances of this class represents an immutable map that associates values of a comparable type <code>K</code>
 * to value of type <code>V</code>.
 *
 * @param <K> types of keys.
 * @param <V> types of values.
 */
public final class SibillaMap<K extends Comparable<K>, V> implements Iterable<Map.Entry<K,V>> {

    /**
     * The internal data structure used to store bindings in this map.
     */
    private final Node<K, V> node;

    /**
     * Creates a new empty map.
     */
    public SibillaMap() {
        this(null);
    }

    /**
     * Creates a new map with the given internal structure
     *
     * @param node the internal structure containing the data in the map.
     */
    private SibillaMap(Node<K, V> node) {
        this.node = (node != null ? node.rebalance() : null);
    }


    /**
     * Returns the {@link SibillaMap} containing the same bindings of the given map.
     *
     * @param map a map associating keys of type K to values of type V
     * @param <K> type of keys in the map
     * @param <V> type of values in the map
     * @return the {@link SibillaMap} containing the same bindings of the given map.
     */
    public static <K extends Comparable<K>, V> SibillaMap<K, V> of(Map<K, V> map) {
        SibillaMap<K, V> newMap = new SibillaMap<>();
        for (Map.Entry<K, V> e : map.entrySet()) {
            newMap = newMap.add(e);
        }
        return newMap;
    }

    /**
     * Returns true if the given internal structure contains the given key.
     *
     * @param key the key to search in the map
     * @param map the internal data structure where we are searching the key
     * @return true if the given internal structure contains the given key.
     * @param <K> the type of the key to search
     */
    private static <K extends Comparable<K>> boolean doContainsKey(K key, Node<K, ?> map) {
        return (map != null) && (map.containsKey(key));
    }


    private static <K extends Comparable<K>, V> Optional<V> doGet(K key, Node<K, V> node) {
        if (node == null) {
            return Optional.empty();
        } else {
            return node.get(key);
        }
    }

    private static <K extends Comparable<K>, V> Node<K, V> doAdd(K key, V value, Node<K, V> node) {
        if (node == null) {
            return new Node<>(key, value);
        } else {
            return node.add(key, value).rebalance();
        }
    }

    private static <K extends Comparable<K>, V, T> T doReduce(Node<K, V> node, BiFunction<Map.Entry<K, V>, T, T> function, T initial) {
        if (node == null) return initial;
        return doReduce(node.rightNode, function, function.apply(node.entry, doReduce(node.leftNode, function, initial)));
    }

    private static <K extends Comparable<K>, V, T> T doReduce(Node<K, V> node, BiFunction<K, V, T> extractor, BinaryOperator<T> aggregator, T initial) {
        if (node == null) {
            return initial;
        } else {
            return doReduce(node.rightNode, extractor, aggregator, aggregator.apply(doReduce(node.leftNode, extractor, aggregator, initial), extractor.apply(node.getKey(), node.getValue())));
        }
    }

    private static <K extends Comparable<K>, V> Node<K, V> doApplyOrAddIfNotExists(Node<K, V> node, K key, Function<V, V> function, Supplier<V> supplier) {
        if (node == null) return new Node<>(key, supplier.get());
        return node.applyOrAddIfNotExists(key, function, supplier);
    }

    private static <K extends Comparable<K>, V> Optional<Pair<Map.Entry<K, V>, Node<K, V>>> doRemoveFirst(Node<K, V> node) {
        if (node == null) {
            return Optional.empty();
        } else {
            return node.removeFirst();
        }
    }

    /**
     * Adds to this map the given entry.
     *
     * @param e
     * @return
     */
    private SibillaMap<K, V> add(Map.Entry<K, V> e) {
        return add(e.getKey(), e.getValue());
    }

    /**
     * Returns a new map where the given value is associated with the given key.
     *
     * @param key   key with which the specific value will be associated.
     * @param value value to be associated with the given key.
     * @return a new map where the given value is associated with the given key.
     */
    public SibillaMap<K, V> add(K key, V value) {
        Node<K, V> newNode = doAdd(key, value, node);
        if (newNode == node) return this;
        return new SibillaMap<>(newNode);
    }

    /**
     * Returns the {@link Optional<V>}  containing the value associated to the given key in this map.
     * The result is empty if no value is mapped to the given key in this map.
     *
     * @param key the key whose associated value is to be returned
     * @return the {@link Optional<V>}  containing the value associated to the given key in this map. The result is empty
     * if no value is mapped to the given key in this map.
     */
    public Optional<V> get(K key) {
        return doGet(key, this.node);
    }

    /**
     * Returns true if a value is associated with the given key.
     *
     * @param key the key whose presence in this map is to be tested
     * @return true if a value is associated with the given key.
     */
    public boolean containsKey(K key) {
        return doContainsKey(key, node);
    }

    /**
     * Returns true if this map is empty.
     *
     * @return true if this map is empty.
     */
    public boolean isEmpty() {
        return node == null;
    }

    /**
     * Returns the map obtained from this one by adding all the mappings stored in the given map.
     *
     * @param map the map containing all the mapping to add
     * @return the map obtained from this one by adding all the mappings stored in the given map.
     */
    public SibillaMap<K, V> addAll(Map<K, V> map) {
        SibillaMap<K, V> newMap = this;
        for (Map.Entry<K, V> e : map.entrySet()) {
            newMap = newMap.add(e);
        }
        return newMap;
    }

    /**
     * Returns the map obtained from this one by applying to all its values the given function.
     *
     * @param function the function to apply to the values stored in this map.
     * @param <T>      type of the values stored in the resulting map.
     * @return the map obtained from this one by applying to all its values the given function.
     */
    public <T> SibillaMap<K, T> apply(Function<? super V, ? extends T> function) {
        return new SibillaMap<>(doApply(node, function));
    }

    /**
     * Returns the map obtained from this one by applying to all its values the given function.
     *
     * @param function the function to apply to the values stored in this map.
     * @param <T>      type of the values stored in the resulting map.
     * @return the map obtained from this one by applying to all its values the given function.
     */
    public <T> SibillaMap<K, T> apply(BiFunction<K, V, T> function) {
        return new SibillaMap<>(doApply(node, function));
    }

    public SibillaMap<K,V> apply(K key, UnaryOperator<V> function) {
        return new SibillaMap<>(doApply(key, function, this.node));
    }

    public SibillaMap<K,V> apply(UnaryOperator<V> function) {
        return new SibillaMap<>(doApply(function, this.node));
    }

    private static <K extends Comparable<K>, V> Node<K, V> doApply(UnaryOperator<V> function, Node<K, V> node) {
        if (node == null) {
            return null;
        } else {
            return node.apply(function);
        }
    }

    public <T> Optional<Pair<T,SibillaMap<K, V>>> apply(K key, Function<V,Optional<Pair<T,V>>> function) {
        return doApply(key, function, this.node).map(p -> p.applyToSecond(SibillaMap::new));
    }

    private static <K extends Comparable<K>, V, T> Optional<Pair<T, Node<K,V>>> doApply(K key, Function<V, Optional<Pair<T,V>>> function, Node<K,V> node) {
        if (node == null) {
            return Optional.empty();
        } else {
            return node.apply(key, function);
        }
    }


    private static <K extends Comparable<K>, V> Node<K,V> doApply(K key, UnaryOperator<V> function, Node<K,V> node) {
        if (node == null) {
            return null;
        }
        return node.apply(key, function);
    }

    private <T> Node<K, T> doApply(Node<K, V> node, BiFunction<K, V, T> function) {
        if (node == null) return null;
        return new Node<>(node.getKey(), function.apply(node.getKey(), node.getValue()), doApply(node.leftNode, function), doApply(node.rightNode, function));
    }

    private <T> Node<K, T> doApply(Node<K, V> node, Function<? super V, ? extends T> function) {
        if (node == null) {
            return null;
        } else {
            return new Node<>(node.getKey(), function.apply(node.getValue()), doApply(node.leftNode, function), doApply(node.rightNode, function));
        }
    }

    /**
     * Returns the value obtained by aggregating the values in the map by using the given functions. Let v1, v2,..., vk
     * be the values stored in this map, the result of reduce(e, a, v) is equal to:
     * <p>
     * a(...(a(v, f(v1)), f(v2))...), f(vk))
     *
     * @param extractor  a function used to extract a value of type T to the values stored in the map
     * @param aggregator function used to aggregate the values computed in the map
     * @param initial    the initial value of the aggregation.
     * @param <T>        type of the aggregated value
     * @return the value obtained by aggregating the values in the map by using the given functions
     */
    public <T> T reduce(BiFunction<K, V, T> extractor, BinaryOperator<T> aggregator, T initial) {
        return doReduce(this.node, extractor, aggregator, initial);
    }

    /**
     * Returns the value obtained by aggregating the values in the map by using the given functions.
     * Let [ k1 -> v1, k2 -> v2,..., kn -> vn ] be the values stored in this map, the result of
     * reduce(f, v) is equal to:
     * <p>
     * f(kn,vn,f((......(fk2, v2, f(k1, v1, v))...)
     *
     * @param function function used to aggregate values
     * @param initial  initial value of the aggregation
     * @param <T>      type of the resulting value
     * @return the value obtained by aggregating the values in the map by using the given functions.
     */
    public <T> T reduce(BiFunction<Map.Entry<K, V>, T, T> function, T initial) {
        return doReduce(this.node, function, initial);
    }

    /**
     * This method is used to iterate through all the map by performing the given
     * action on each stored binding. The elements are handled in the ascending order.
     *
     * @param consumer the action to apply to each binding in the map.
     */
    public void iterate(BiConsumer<K, V> consumer) {
        doIterate(this.node, consumer);
    }

    private void doIterate(Node<K, V> node, BiConsumer<K, V> consumer) {
        if (node != null) {
            doIterate(node.leftNode, consumer);
            consumer.accept(node.getKey(), node.getValue());
            doIterate(node.rightNode, consumer);
        }
    }

    /**
     * Returns the least key stored in this map.
     *
     * @return the least key stored in this map.
     */
    public Optional<K> getMinKey() {
        if (this.node == null) {
            return Optional.empty();
        } else {
            return this.node.getMinKey();
        }
    }

    /**
     * Returns the greatest key stored in this map.
     *
     * @return the greatest key stored in this map.
     */
    public Optional<K> getMaxKey() {
        if (this.node == null) {
            return Optional.empty();
        } else {
            return this.node.getMaxKey();
        }
    }

    /**
     * Returns the map obtained by this one by applying the given function to the value associated with the given
     * key. If this key is not present in this map, a new binding is created to the value returned by the given
     * supplier.
     *
     * @param key      the key whose value is updated or added
     * @param function the function used to compute the new value
     * @param supplier the supplier used to obtain the new value to bind to the given key
     * @return the map obtained by this one by applying the given function to the value associated with the given
     * key. If this key is not present in this map, a new binding is created to the value returned by the given
     * supplier.
     */
    public SibillaMap<K, V> applyOrAddIfNotExists(K key, Function<V, V> function, Supplier<V> supplier) {
        Node<K, V> node = doApplyOrAddIfNotExists(this.node, key, function, supplier);
        if (this.node == node) return this;
        return new SibillaMap<>(node);
    }

    /**
     * Return an optional containing a pair with the entry the least key in this map and the
     * map obtained from resulting from this one by removing that entry. The optional is empty if
     * this map is empty.
     *
     * @return an optional containing a pair with the entry the least key in this map and the
     * map obtained from resulting from this one by removing that entry.
     */
    public Optional<Pair<Map.Entry<K, V>, SibillaMap<K, V>>> removeFirst() {
        return doRemoveFirst(this.node).map(Pair.combine(e -> e, SibillaMap::new));
    }

    public V getOrDefault(K key, V defaultValue) {
        return get(key).orElse(defaultValue);
    }

    private Queue<Node<K, V>> getQueueToFirstNode() {
        Queue<Node<K, V>> queue = new LinkedList<>();
        Node<K,V> current = this.node;
        while (current != null) {
            queue.add(current);
            current = current.leftNode;
        }
        return queue;
    }

    public int size() {
        if (this.node == null) {
            return 0;
        } else {
            return this.node.size();
        }
    }

    public SibillaMap<K, V> remove(K key) {
        return new SibillaMap<>(doRemove(this.node, key));
    }

    private static <K extends Comparable<K>, V> Node<K,V> doRemove(Node<K,V> node, K key) {
        if (node == null) {
            return null;
        } else {
            return node.remove(key);
        }
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return getEntriesIterator();
    }

    public static class Node<K extends Comparable<K>, V> {

        private final Map.Entry<K, V> entry;

        private final Node<K, V> leftNode;

        private final Node<K, V> rightNode;
        private final int height;
        private final int balance;

        private final int size;

        Node(K key, V value, Node<K, V> leftNode, Node<K, V> rightNode) {
            this(Map.entry(key, value), leftNode, rightNode);
        }

        public Node(Map.Entry<K, V> entry, Node<K, V> leftNode, Node<K, V> rightNode) {
            this.entry = entry;
            this.leftNode = leftNode;
            this.rightNode = rightNode;
            this.height = 1 + Math.max(heightOf(leftNode), heightOf(rightNode));
            this.balance = heightOf(rightNode) - heightOf(leftNode);
            this.size = sizeOf(leftNode)+sizeOf(rightNode)+1;
        }

        Node(K key, V value) {
            this(key, value, null, null);
        }

        private int heightOf(Node<K, V> node) {
            return (node == null ? -1 : node.height);
        }

        private int sizeOf(Node<K, V> node) {
            return (node == null ? 0 : node.size);
        }

        public K getKey() {
            return entry.getKey();
        }

        public V getValue() {
            return entry.getValue();
        }

        public Node<K, V> add(K key, V value) {
            int compare = this.getKey().compareTo(key);
            if (compare == 0) {
                if (Objects.equals(this.getValue(), value)) {
                    return this;
                } else {
                    return new Node<>(this.getKey(), value, leftNode, rightNode);
                }
            }
            if (compare < 0) {
                Node<K, V> newNode = doAdd(key, value, rightNode);
                if (newNode == rightNode) return this;
                return new Node<>(this.getKey(), this.getValue(), leftNode, newNode);
            } else {
                Node<K, V> newNode = doAdd(key, value, leftNode);
                if (newNode == leftNode) return this;
                return new Node<>(this.getKey(), this.getValue(), newNode, rightNode);
            }
        }

        private Node<K, V> rebalance() {
            if (Math.abs(this.balance) < 2) {
                return this;
            }
            if (heightOf(leftNode) > heightOf(rightNode)) {
                if (leftNode.balance < 0) {
                    return rotateRight();
                } else {
                    return rotateLeftRight();
                }
            } else {
                if (rightNode.balance > 0) {
                    return rotateLeft();
                } else {
                    return rotateRightLeft();
                }
            }
        }

        private Node<K, V> rotateLeftRight() {
            return new Node<>(
                    leftNode.rightNode.getKey(), leftNode.rightNode.getValue(),
                    new Node<>(leftNode.getKey(), leftNode.getValue(), leftNode.leftNode, leftNode.rightNode.leftNode),
                    new Node<>(getKey(), getValue(), leftNode.rightNode.leftNode, rightNode)
            );
        }

        private Node<K, V> rotateRightLeft() {
            return new Node<>(
                    rightNode.leftNode.getKey(), rightNode.leftNode.getValue(),
                    new Node<>(getKey(), getValue(), leftNode, rightNode.leftNode.leftNode),
                    new Node<>(rightNode.getKey(), rightNode.getValue(), rightNode.leftNode.rightNode, rightNode.rightNode)
            );
        }

        public Optional<V> get(K key) {
            int compare = this.getKey().compareTo(key);
            if (compare == 0) {
                return Optional.of(getValue());
            }
            if (compare < 0) {
                return doGet(key, rightNode);
            } else {
                return doGet(key, leftNode);
            }
        }


        public Node<K, V> rotateLeft() {
            return new Node<>(rightNode.getKey(), rightNode.getValue(),
                    new Node<>(this.getKey(), this.getValue(), this.leftNode, rightNode.leftNode),
                    this.rightNode.rightNode);
        }

        public Node<K, V> rotateRight() {
            return new Node<>(leftNode.getKey(), leftNode.getValue(),
                    this.leftNode.leftNode,
                    new Node<>(this.getKey(), this.getValue(), this.leftNode.rightNode, this.rightNode));
        }

        public boolean containsKey(K key) {
            int compare = this.getKey().compareTo(key);
            if (compare == 0) return true;
            if (compare < 0) return doContainsKey(key, rightNode);
            return doContainsKey(key, leftNode);
        }

        public Optional<K> getMinKey() {
            if (leftNode == null) {
                return Optional.of(getKey());
            } else {
                return leftNode.getMinKey();
            }
        }

        public Optional<K> getMaxKey() {
            if (rightNode == null) {
                return Optional.of(getKey());
            } else {
                return rightNode.getMaxKey();
            }
        }

        public Node<K, V> applyOrAddIfNotExists(K key, Function<V, V> function, Supplier<V> supplier) {
            int compare = this.getKey().compareTo(key);
            if (compare == 0) {
                V value = function.apply(getValue());
                if (Objects.equals(this.getValue(), value)) {
                    return this;
                } else {
                    return new Node<>(this.getKey(), value, leftNode, rightNode);
                }
            }
            if (compare < 0) {
                Node<K, V> newNode = doApplyOrAddIfNotExists(rightNode, key, function, supplier);
                if (newNode == rightNode) return this;
                return new Node<>(this.getKey(), this.getValue(), leftNode, newNode);
            } else {
                Node<K, V> newNode = doApplyOrAddIfNotExists(leftNode, key, function, supplier);
                if (newNode == leftNode) return this;
                return new Node<>(key, getValue(), newNode, rightNode);
            }
        }

        public Optional<Pair<Map.Entry<K, V>, Node<K, V>>> removeFirst() {
            if (this.leftNode == null) {
                return Optional.of(Pair.of(this.entry, this.rightNode));
            }
            return this.leftNode.removeFirst().map(Pair.combine(e -> e, n -> new Node<>(this.entry, n.rebalance(), rightNode)));
        }

        public Node<K,V> apply(K key, UnaryOperator<V> function) {
            int compare = this.entry.getKey().compareTo(key);
            if (compare == 0) {
                return new Node<>(Map.entry(entry.getKey(), function.apply(this.entry.getValue())), leftNode, rightNode);
            }
            if (compare < 0) {
                return new Node<>(this.entry, leftNode, doApply(key, function, rightNode));
            }
            return new Node<>(this.entry, doApply(key, function, leftNode), rightNode);
        }

        public <T> Optional<Pair<T, Node<K,V>>> apply(K key, Function<V, Optional<Pair<T, V>>> function) {
            int compare = this.entry.getKey().compareTo(key);
            if (compare == 0) {
                return function.apply(entry.getValue()).map(p -> p.applyToSecond(this::set));
            }
            if (compare < 0) {
                return doApply(key, function, this.rightNode).map(p -> p.applyToSecond(this::setRightNode));
            }
            return doApply(key, function, this.rightNode).map(p -> p.applyToSecond(this::setLeftNode));
        }

        private Node<K,V> set(V value) {
            return new Node<>(this.getKey(), value, leftNode, rightNode);
        }

        private Node<K, V> setLeftNode(Node<K, V> node) {
            return new Node<>(this.entry, node, rightNode).rebalance();
        }

        private Node<K, V> setRightNode(Node<K, V> node) {
            return new Node<>(this.entry, leftNode, node).rebalance();
        }

        public Node<K,V> apply(UnaryOperator<V> function) {
            return new Node<>(getKey(), function.apply(getValue()), doApply(function, leftNode), doApply(function, rightNode));
        }

        public int size() {
            return size;
        }

        public Node<K,V> remove(K key) {
            int compare = this.getKey().compareTo(key);
            if (compare == 0) {
                if (rightNode == null) return leftNode;
                Optional<Pair<Map.Entry<K, V>, Node<K, V>>> entryNodePair = rightNode.removeFirst();
                return entryNodePair.map(p -> new Node<>(p.getKey(), this.leftNode, p.getValue()).rebalance()).orElse(leftNode);
            }
            if (compare < 0) {
                return new Node<>(this.entry, leftNode, doRemove(rightNode, key)).rebalance();
            } else {
                return new Node<>(this.entry, doRemove(leftNode, key), rightNode).rebalance();
            }
        }
    }

    public Iterator<K> getKeysIterator() {
        return new SibillaMapIterator<>(this.node, Map.Entry::getKey);
    }

    public Iterator<V> getValuesIterator() {
        return new SibillaMapIterator<>(this.node, Map.Entry::getValue);
    }

    public Iterator<Map.Entry<K, V>> getEntriesIterator() {
        return new SibillaMapIterator<>(this.node, e -> e);
    }

    public Stream<K> streamOfKeys() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.getKeysIterator(), Spliterator.NONNULL), false);
    }

    public Stream<V> streamOfValues() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.getValuesIterator(), Spliterator.NONNULL), false);
    }

    public Stream<Map.Entry<K,V>> streamOfEntries() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.getEntriesIterator(), Spliterator.NONNULL), false);
    }


    static class SibillaMapIterator<K extends Comparable<K>, V, T> implements Iterator<T> {

        private final Queue<Node<K, V>> queue;

        private final Function<Map.Entry<K,V>,T> extractor;

        public SibillaMapIterator(Node<K, V> startingNode, Function<Map.Entry<K, V>, T> extractor) {
            this.extractor = extractor;
            queue = new LinkedList<>();
            enqueToMinNode(startingNode);
        }

        private void enqueToMinNode(Node<K,V> startingNode) {
            Node<K, V> current = startingNode;
            while (current != null) {
                queue.add(current);
                current = current.leftNode;
            }
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public T next() {
            Node<K, V> nextNode = queue.poll();
            if (nextNode == null) throw new NoSuchElementException();
            T result = extractor.apply(nextNode.entry);
            enqueToMinNode(nextNode.rightNode);
            return result;
        }
    }
}
