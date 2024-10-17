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

package it.unicam.quasylab.sibilla.tools.glotl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ChachedFunction<T,R> implements Function<T, R> {

    private final Function<T,R> function;
    private final Map<T, R> cache;


    public ChachedFunction(Function<T, R> function) {
        this.function = function;
        this.cache = new HashMap<>();
    }


    @Override
    public R apply(T t) {
        //System.out.println(cache.size());
        return cache.computeIfAbsent(t, function);
    }

    public int size() {
        return cache.size();
    }
}
