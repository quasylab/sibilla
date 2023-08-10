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

package it.unicam.quasylab.sibilla.core.util;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Instances of this class represents an immutable map that associates values of a comparable type <code>K</code>
 * to value of type <code>V</code>.
 *
 * @param <K> types of keys.
 * @param <V> types of values.
 */
public final class SibillaMap<K extends Comparable<K>,V> {

    private final Node<K,V> node;

    /**
     * Creates a new empty map.
     */
    public SibillaMap() {
        this(null);
    }

    private SibillaMap(Node<K, V> node) {
        this.node = node;
    }

    public static <K extends Comparable<K>, V> SibillaMap<K,V> of(Map<K, V> map) {
        SibillaMap<K, V> newMap = new SibillaMap<>();
        for (Map.Entry<K, V> e: map.entrySet()) {
            newMap = newMap.add(e);
        }
        return newMap;
    }

    private SibillaMap<K,V> add(Map.Entry<K, V> e) {
        return add(e.getKey(), e.getValue());
    }

    /**
     * Returns a new map where the given value is associated with the given key.
     *
     * @param key key with which the specific value will be associated.
     * @param value value to be associated with the given key.
     * @return a new map where the given value is associated with the given key.
     */
    public SibillaMap<K,V> add(K key, V value) {
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

    private static <K extends Comparable<K>> boolean doContainsKey(K key, Node<K,?> map) {
        return (map != null)&&(map.containsKey(key));
    }

    private static <K extends Comparable<K>,  V> Optional<V> doGet(K key, Node<K,V> node) {
        if (node == null) {
            return Optional.empty();
        } else {
            return node.get(key);
        }
    }

    private static <K extends Comparable<K>, V> Node<K,V> doAdd(K key, V value, Node<K,V> node) {
        if (node == null) {
            return new Node<>(key, value);
        } else {
            Node<K, V> newNode = node.add(key, value);
            if (Math.abs(newNode.balance)<2) {
                return newNode;
            } else {
                return newNode.rebalance();
            }
        }
    }

    /**
     * Returns the map obtained from this one by adding all the mappings stored in the given map.
     *
     * @param map the map containing all the mapping to add
     * @return the map obtained from this one by adding all the mappings stored in the given map.
     */
    public SibillaMap<K, V> addAll(Map<K, V> map) {
        SibillaMap<K, V> newMap = this;
        for (Map.Entry<K, V> e: map.entrySet()) {
            newMap = newMap.add(e);
        }
        return newMap;
    }

    public <T> SibillaMap<K, T> apply(Function<? super V, ? extends T> function) {
        return new SibillaMap<>(doApply(node, function));
    }

    public <T> SibillaMap<K, T> apply(BiFunction<K,V, T> function) {
        return new SibillaMap<>(doApply(node, function));
    }

    private <T> Node<K, T> doApply(Node<K,V> node, BiFunction<K,V,T> function) {
        if (node == null) return null;
        return new Node<>(node.getKey(), function.apply(node.getKey(), node.getValue()), doApply(node.leftNode, function), doApply(node.rightNode, function));
    }

    private <T> Node<K, T> doApply(Node<K, V> node, Function<? super V,? extends T> function) {
        if (node == null) {
            return null;
        } else {
            return new Node<>(node.getKey(), function.apply(node.getValue()), doApply(node.leftNode, function), doApply(node.rightNode, function));
        }
    }

    public <T> T reduce(BiFunction<K, V, T> extractor, BinaryOperator<T> aggregator, T initial) {
        return doReduce(this.node, extractor, aggregator, initial);
    }

    public <T> T reduce(BiFunction<Map.Entry<K, V>, T, T> function, T initial) {
        return doReduce(this.node, function, initial);
    }

    private static <K extends Comparable<K>, V, T> T doReduce(Node<K,V> node, BiFunction<Map.Entry<K,V>,T,T> function, T initial) {
        if (node == null) return initial;
        return doReduce(node.rightNode, function, function.apply(node.entry, doReduce(node.leftNode, function, initial)));
    }


    private static <K extends Comparable<K>, V, T> T doReduce(Node<K,V> node, BiFunction<K,V,T> extractor, BinaryOperator<T> aggregator, T initial) {
        if (node == null) {
            return initial;
        } else {
            return doReduce(node.rightNode, extractor, aggregator, aggregator.apply(doReduce(node.leftNode, extractor, aggregator, initial), extractor.apply(node.getKey(), node.getValue())));
        }
    }

    public void iterate(BiConsumer<K, V> consumer) {
        doIterate(this.node, consumer);
    }

    private void doIterate(Node<K,V> node, BiConsumer<K,V> consumer) {
        if (node != null) {
            doIterate(node.leftNode, consumer);
            consumer.accept(node.getKey(), node.getValue());
            doIterate(node.rightNode, consumer);
        }
    }


    static class Node<K extends Comparable<K>, V> {

         private final Map.Entry<K, V> entry;

         private final Node<K,V> leftNode;

         private final Node<K,V> rightNode;
         private final int height;
         private final int balance;

         Node(K key, V value, Node<K,V> leftNode, Node<K,V> rightNode) {
             this.entry = Map.entry(key, value);
             this.leftNode = leftNode;
             this.rightNode = rightNode;
             this.height = 1+Math.max(heightOf(leftNode), heightOf(rightNode));
             this.balance = heightOf(rightNode) - heightOf(leftNode);
         }

         private int heightOf(Node<K,V> node) {
             return (node==null?-1:node.height);
         }

         Node(K key, V value) {
            this(key, value, null, null);
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
                 return new Node<>(key, value, newNode, rightNode);
             }
        }

         private Node<K,V> rebalance() {
             if (heightOf(leftNode)>heightOf(rightNode)) {
                 if (leftNode.balance<0) {
                     return rotateRight();
                 } else {
                     return rotateLeftRight();
                 }
             } else {
                 if (rightNode.balance>0) {
                     return rotateLeft();
                 } else {
                     return rotateRightLeft();
                 }
             }
         }

         private Node<K,V> rotateLeftRight() {
             return new Node<>(
                     leftNode.rightNode.getKey(), leftNode.leftNode.getValue(),
                     new Node<>(leftNode.getKey(),leftNode.getValue(), leftNode.leftNode, leftNode.rightNode.leftNode),
                     new Node<>(getKey(), getValue(), leftNode.rightNode.leftNode, rightNode)
             );
         }

         private Node<K,V> rotateRightLeft() {
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



         public Node<K,V> rotateLeft() {
             return new Node<>(rightNode.getKey(), rightNode.getValue(),
                     new Node<>(this.getKey(), this.getValue(), this.leftNode, rightNode.leftNode),
                     this.rightNode.rightNode);
         }

         public Node<K,V> rotateRight() {
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
    }

}
