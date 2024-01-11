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

import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.DoublePredicate;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class SequenceOfPositiveIntervalsTest {

    private static final List<Interval> INTERVALS1 = List.of(new Interval(0, 10), new Interval(20, 30));
    private static final List<Interval> INTERVALS2 = List.of(new Interval(0, 15), new Interval(20, 50));

    private static final List<Interval> INTERVALS3 = List.of(new Interval(5, 16), new Interval(18, 23), new Interval(35, 45));

    private static final List<Interval> INTERVALS4 = List.of(new Interval(1, 20), new Interval(22, 25), new Interval(30, 50));

    private final DoublePredicate SIN_GENERATOR = d -> Math.sin(d)>0;



    public static BooleanSignal getSequence(double dt, int size, DoublePredicate predicate) {
        BooleanSignal sequence = new BooleanSignal();
        for(int i=0; i<size; i++) {
            if (predicate.test(i*dt)) {
                sequence.add(i*dt, (i+1)*dt);
            }
        }
        return sequence;
    }


    @Test
    void negate() {
        BooleanSignal sequence = BooleanSignal.of(INTERVALS1);
        BooleanSignal negatedSequence = sequence.negate();
        assertEquals(2,negatedSequence.size());
        assertEquals(new Interval(10, 20), negatedSequence.get(0));
        assertEquals(new Interval(30, Double.POSITIVE_INFINITY), negatedSequence.get(1));
    }

    @Test
    void shift() {
        BooleanSignal sequence = BooleanSignal.of(INTERVALS2);
        BooleanSignal shiftedSequence = sequence.shift(new Interval(5, 10));
        assertEquals(1,shiftedSequence.size());
        assertEquals(new Interval(0, 45), shiftedSequence.get(0));
    }



    @Test
    void getValuesAt() {
        BooleanSignal sequence = getSequence(1.5, 100, SIN_GENERATOR);
        double[] steps = IntStream.range(0, 100).mapToDouble(i -> i*1.5).toArray();
        boolean[] values = sequence.getValuesAt(steps);
        for(int i=0 ; i<values.length; i++) {
            assertEquals(SIN_GENERATOR.test(i*1.5), values[i], "index: "+i);
        }

    }

    @Test
    void computeConjunction() {
        BooleanSignal sequence1 = BooleanSignal.of(INTERVALS1);
        BooleanSignal sequence2 = BooleanSignal.of(INTERVALS3);
        BooleanSignal sequence3 = sequence1.computeConjunction(sequence2);
        assertEquals(2, sequence3.size());
        assertEquals(new Interval(5, 10), sequence3.get(0));
        assertEquals(new Interval(20, 23), sequence3.get(1));
    }

    @Test
    void computeConjunction2() {
        BooleanSignal sequence1 = BooleanSignal.of(INTERVALS3);
        BooleanSignal sequence2 = BooleanSignal.of(INTERVALS4);
        BooleanSignal sequence3 = sequence1.computeConjunction(sequence2);
        assertEquals(4, sequence3.size());
        assertEquals(new Interval(5, 16), sequence3.get(0));
        assertEquals(new Interval(18, 20), sequence3.get(1));
        assertEquals(new Interval(22, 23), sequence3.get(2));
        assertEquals(new Interval(35, 45), sequence3.get(3));
    }

    @Test
    void computeDisjunction() {
        BooleanSignal sequence1 = BooleanSignal.of(INTERVALS1);
        BooleanSignal sequence2 = BooleanSignal.of(INTERVALS3);
        BooleanSignal sequence3 = sequence1.computeDisjunction(sequence2);
        assertEquals(3, sequence3.size());
        assertEquals(new Interval(0, 16), sequence3.get(0));
        assertEquals(new Interval(18, 30), sequence3.get(1));
        assertEquals(new Interval(35, 45), sequence3.get(2));
    }
}