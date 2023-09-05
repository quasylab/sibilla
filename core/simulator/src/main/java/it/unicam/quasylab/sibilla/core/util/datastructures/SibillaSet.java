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


import java.util.Iterator;

/**
 * This class represents an immutable set.
 */
public class SibillaSet<T extends Comparable<T>> implements Iterable<T> {

    private final static Object PRESENT = new Object();

    private final SibillaMap<T, Object> sibillaMap;


    public SibillaSet(SibillaMap<T, Object> sibillaMap) {
        this.sibillaMap = sibillaMap;
    }

    public static <T extends Comparable<T>> SibillaSet<T> of(T element) {
        return new SibillaSet<>(new SibillaMap<T, Object>().add(element, PRESENT));
    }

    public SibillaSet<T> add(T element) {
        return new SibillaSet<>(this.sibillaMap.add(element, PRESENT));
    }

    public SibillaSet<T> remove(T element) {
        return new SibillaSet<>(this.sibillaMap.remove(element));
    }

    @Override
    public Iterator<T> iterator() {
        return this.sibillaMap.getKeysIterator();
    }
}
