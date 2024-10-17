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

import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * This is a utility class that implements a <code>Spliterator<ProbabilityEntries<S>></code>
 * on top of a Spliterator<Map.Entry<S, Double>>.
 *
 * @param <S> type of measured elements
 */
class ProbabilityEntrySplitIterator<S> implements Spliterator<ProbabilityEntries<S>> {


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
