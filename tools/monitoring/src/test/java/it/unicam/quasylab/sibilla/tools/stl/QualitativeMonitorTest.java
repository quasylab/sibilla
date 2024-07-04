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

package it.unicam.quasylab.sibilla.tools.stl;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.tools.stl.QualitativeMonitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static it.unicam.quasylab.sibilla.tools.stl.CommonForMonitorTesting.getPopulationTrajectory;
import static org.junit.jupiter.api.Assertions.*;


class QualitativeMonitorTest {

    @Test
    public void testAtomicMonitor(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        BooleanSignal bs =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 2 ).monitor(t);


        assertFalse(bs.getValueAt(0.5));
        assertTrue(bs.getValueAt(1.0));
        assertTrue(bs.getValueAt(3.5));
        assertFalse(bs.getValueAt(4.0));
        assertFalse(bs.getValueAt(4.5));
    }


    @Test
    public void testNegationMonitor(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        QualitativeMonitor<PopulationState> bs =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 2 );
        BooleanSignal nbs = QualitativeMonitor.negation(bs).monitor(t);
        assertTrue(nbs.getValueAt(0.5));
        assertFalse(nbs.getValueAt(1.0));
        assertFalse(nbs.getValueAt(3.5));
        assertTrue(nbs.getValueAt(4.0));
        assertTrue(nbs.getValueAt(4.5));


    }

    /**
     *  s1 AND s2 expected [ [ 5.0 , 6.0 ) ]
     *  s1 OR  s2 expected [ [ 1.0 , 7.0 ) ]
     */
    @Test
    public void testConjunctionAndDisjunction(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 2, 3, 1},
                new int[]{2, 6, 1, 1, 1, 1, 0}
        );

        Trajectory<PopulationState> t2 = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 2, 3, 1},
                new int[]{2, 6, 1, 1, 1, 0, 0}
        );


        QualitativeMonitor<PopulationState> leftAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 2 );
        QualitativeMonitor<PopulationState> rightAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(1) <= 0 );

        QualitativeMonitor<PopulationState> conjunction = QualitativeMonitor.conjunction(leftAtomic,rightAtomic);
        QualitativeMonitor<PopulationState> disjunction= QualitativeMonitor.disjunction(leftAtomic,rightAtomic);

        BooleanSignal cbs = conjunction.monitor(t);
        BooleanSignal dbs = disjunction.monitor(t);
        BooleanSignal c2bs = conjunction.monitor(t2);


        assertEquals(0, cbs.getIntervals().size());

        assertFalse(c2bs.getValueAt(1.5));
        assertFalse(c2bs.getValueAt(2.5));
        assertFalse(c2bs.getValueAt(3.5));
        assertFalse(c2bs.getValueAt(4.5));
        assertTrue(c2bs.getValueAt(5.5));
        assertFalse(c2bs.getValueAt(6.0));


        assertFalse(dbs.getValueAt(0.5));
        assertTrue(dbs.getValueAt(2.5));
        assertTrue(dbs.getValueAt(3.5));
        assertTrue(dbs.getValueAt(4.5));
        assertTrue(dbs.getValueAt(5.5));
        assertTrue(dbs.getValueAt(6.0));
        assertFalse(dbs.getValueAt(7.5));
    }


    @Test
    public void testConjunctionAndDisjunctionDisjointInterval(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{5, 5, 5, 1, 1, 1, 1},
                new int[]{1, 1, 1, 1, 5, 5, 5}
        );

        QualitativeMonitor<PopulationState> leftAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) > 3 );
        QualitativeMonitor<PopulationState> rightAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(1) > 3 );

        QualitativeMonitor<PopulationState> disjunction = QualitativeMonitor.disjunction(leftAtomic,rightAtomic);
        QualitativeMonitor<PopulationState> conjunction = QualitativeMonitor.conjunction(leftAtomic,rightAtomic);

        assertEquals(disjunction.monitor(t).size(),2);
        assertTrue(conjunction.monitor(t).isEmpty());
    }


    @Test
    public void testConjunctionAndDisjunctionEmptyOne(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{5, 5, 5, 1, 1, 1, 1},
                new int[]{1, 1, 1, 1, 1, 1, 1}
        );

        QualitativeMonitor<PopulationState> leftAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) > 3 );
        QualitativeMonitor<PopulationState> rightAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(1) > 3 );

        QualitativeMonitor<PopulationState> disjunction = QualitativeMonitor.disjunction(leftAtomic,rightAtomic);
        QualitativeMonitor<PopulationState> conjunction = QualitativeMonitor.conjunction(leftAtomic,rightAtomic);

        assertEquals(disjunction.monitor(t).size(),1);
        assertTrue(conjunction.monitor(t).isEmpty());
    }


    @Test
    public void testUntil(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 3, 3, 3, 3, 4, 1, 3, 3, 5, 4, 1, 1, 1}
        );

        QualitativeMonitor<PopulationState> leftAtomicMonitor =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) == 3 );
        QualitativeMonitor<PopulationState> rightAtomicMonitor =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 3 );

        QualitativeMonitor<PopulationState> untilMonitor = QualitativeMonitor.until(leftAtomicMonitor,new Interval(0.1),rightAtomicMonitor);
        BooleanSignal untilSignal = untilMonitor.monitor(t);
        assertEquals(untilSignal.getIntervals().size(), 2);
        assertEquals(untilSignal.getIntervals().get(0), new Interval(1.0, 5.0));
        assertEquals(untilSignal.getIntervals().get(1), new Interval(7.0, 9.0));

    }


    @Test
    public void testGlobally(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 3, 3, 3, 3, 4, 1, 3, 3, 5, 4, 1, 1, 1}
        );

        QualitativeMonitor<PopulationState> atomicMonitor =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 3 );


        QualitativeMonitor<PopulationState> globallyMonitor =
                QualitativeMonitor.globally( new Interval(0,1),atomicMonitor);

        BooleanSignal globalSignal = globallyMonitor.monitor(t);
        assertEquals(globalSignal.getIntervals().size(), 2);
        assertEquals(globalSignal.getIntervals().get(0), new Interval(1.0, 5.0));
        assertEquals(globalSignal.getIntervals().get(1), new Interval(7.0, 10.0));

    }


    @Test
    public void testEventually(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 3, 3, 3, 3, 4, 1, 1, 2, 5, 4, 1, 1, 1}
        );

        QualitativeMonitor<PopulationState> atomicMonitor =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 3 );


        QualitativeMonitor<PopulationState> eventuallyMonitor =
                QualitativeMonitor.eventually( new Interval(0,1),atomicMonitor);

        BooleanSignal eventuallySignal = eventuallyMonitor.monitor(t);
        assertEquals(eventuallySignal.getIntervals().get(0), new Interval(0.0, 6.0));
        assertEquals(eventuallySignal.getIntervals().get(1), new Interval(8.0, 11.0));

    }

    @Test
    public void testProbability(){

        Supplier<Trajectory<PopulationState>> trajectorySupplier = () -> {
            List<Trajectory<PopulationState>> trajectoryList = new ArrayList<>();
            trajectoryList.add(getPopulationTrajectory(new int[]{3,3,3,3,3}));
            trajectoryList.add(getPopulationTrajectory(new int[]{3,3,3,3,3}));
            trajectoryList.add(getPopulationTrajectory(new int[]{1,1,1,1,1}));
            Random random = new Random();
            return trajectoryList.get(random.nextInt(trajectoryList.size()));
        };

        QualitativeMonitor<PopulationState> atomicMonitor =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) == 3 );

        double[] timeSteps = {1.0 , 2.0 , 3.0 , 4.0 , 5.0 , 6.0 , 7.0};
        double[] probabilities = QualitativeMonitor.computeProbability(atomicMonitor, trajectorySupplier, 100,timeSteps);
        assertEquals(0.66, probabilities[0], 0.2);
    }

    @Test
    public void testProbabilityWithDoubleArray() {
        Supplier<Trajectory<PopulationState>> trajectorySupplier = () -> {
            List<Trajectory<PopulationState>> trajectoryList = new ArrayList<>();
            trajectoryList.add(getPopulationTrajectory(new int[]{3, 3, 3, 3, 3}));
            trajectoryList.add(getPopulationTrajectory(new int[]{3, 3, 3, 3, 3}));
            trajectoryList.add(getPopulationTrajectory(new int[]{1, 1, 1, 1, 1}));
            Random random = new Random();
            return trajectoryList.get(random.nextInt(trajectoryList.size()));
        };

        QualitativeMonitor<PopulationState> atomicMonitor =
                QualitativeMonitor.atomicFormula((PopulationState s) -> s.getOccupancy(0) == 3);

        double[] timeSteps = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0};
        double[][] results = QualitativeMonitor.computeTimeSeriesProbabilities(atomicMonitor, trajectorySupplier, 100, timeSteps);

        assertEquals(1.0, results[0][0], 0.001); // Time step
        assertEquals(0.66, results[0][1], 0.2);  // Probability
    }

    @Test
    public void testProbabilityWithDTAndDeadline() {
        Supplier<Trajectory<PopulationState>> trajectorySupplier = () -> {
            List<Trajectory<PopulationState>> trajectoryList = new ArrayList<>();
            trajectoryList.add(getPopulationTrajectory(new int[]{3, 3, 3, 3, 3}));
            trajectoryList.add(getPopulationTrajectory(new int[]{3, 3, 3, 3, 3}));
            trajectoryList.add(getPopulationTrajectory(new int[]{1, 1, 1, 1, 1}));
            Random random = new Random();
            return trajectoryList.get(random.nextInt(trajectoryList.size()));
        };

        QualitativeMonitor<PopulationState> atomicMonitor =
                QualitativeMonitor.atomicFormula((PopulationState s) -> s.getOccupancy(0) == 3);

        double dt = 1.0;
        double deadline = 5.0;
        double[][] results = QualitativeMonitor.computeTimeSeriesProbabilities(atomicMonitor, trajectorySupplier, 100, dt, deadline);

        assertEquals(1.0, results[1][0], 0.001); // Time step
        assertEquals(0.66, results[0][1], 0.2);  // Probability
    }


}