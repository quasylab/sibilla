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

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.core.util.Signal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

public class TestStlParser {


    public static Trajectory<PopulationState> getPopulationTrajectoryAtTimes(double[] times, int[]... signals) {
        if (signals.length == 0)
            throw new IllegalArgumentException("At least one trajectory is needed");
        if (times.length != signals[0].length)
            throw new IllegalArgumentException("The number of time points must match the number of signal points");

        Trajectory<PopulationState> trajectory = new Trajectory<>();
        int numPopulations = signals.length;

        for (int i = 0; i < times.length; i++) {
            Population[] populations = new Population[numPopulations];

            for (int j = 0; j < numPopulations; j++) {
                populations[j] = new Population(j, signals[j][i]);
            }

            trajectory.add(times[i], new PopulationState(numPopulations, populations));
        }

        trajectory.setEnd(times[times.length - 1]);

        return trajectory;
    }

    public static Trajectory<PopulationState> getPopulationTrajectory(double[] timeIntervals, int[]... signals) {
        if(signals.length==0)
            throw new IllegalArgumentException("At least one trajectory is needed");

        Trajectory<PopulationState> trajectory = new Trajectory<>();
        double time = 0.0;

        int numPopulations = signals.length;

        for (int i = 0; i < timeIntervals.length; i++) {
            double currentTimeInterval = timeIntervals[i];
            Population[] populations = new Population[numPopulations];

            for (int j = 0; j < numPopulations; j++) {
                populations[j] = new Population(j, signals[j][i]);
            }

            trajectory.add(time, new PopulationState(numPopulations, populations));
            time += currentTimeInterval;
        }

        trajectory.setEnd(time);

        return trajectory;
    }

    public static  Trajectory<PopulationState> getPopulationTrajectory(int[]... signals) {
        double[] defaultTimeIntervals = new double[signals[0].length];
        Arrays.fill(defaultTimeIntervals, 1.0);
        return getPopulationTrajectory(defaultTimeIntervals,signals);
    }


    @Test
    public void testParsedAtomicFormula() throws StlModelGenerationException {

        String TEST_FORMULA = " measure mes_1 measure mes_2 formula atomicF : [mes_1 > 3] endformula";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));


        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitor =
                stlModelFactory.getQuantitativeMonitor("atomicF");

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        Signal signal = QuantitativeMonitor
                .atomicFormula((PopulationState sign)-> sign.getOccupancy(0) - 3.0).monitor(t);

        Signal signalParsed = parsedQuantitativeMonitor.monitor(t);

        assertEquals(signal.getStart(),signalParsed.getStart());
        assertEquals(signal.getEnd(),signalParsed.getEnd());

        assertEquals(signal.valueAt(0.0),signalParsed.valueAt(0.0));
        assertEquals(signal.valueAt(1.0),signalParsed.valueAt(1.0));
        assertEquals(signal.valueAt(2.0),signalParsed.valueAt(2.0));
        assertEquals(signal.valueAt(3.0),signalParsed.valueAt(3.0));
        assertEquals(signal.valueAt(4.0),signalParsed.valueAt(4.0));
        assertEquals(signal.valueAt(5.0),signalParsed.valueAt(5.0));
        assertEquals(signal.valueAt(6.0),signalParsed.valueAt(6.0));
        assertEquals(signal.valueAt(7.0),signalParsed.valueAt(7.0));

    }

    @Test
    public void testParsedAtomicFormulaWithParameters() throws StlModelGenerationException {

        String TEST_FORMULA = " measure mes_1  formula atomicF [a=3] : [mes_1 > a] endformula";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));

        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitor =
                stlModelFactory.getQuantitativeMonitor("atomicF");

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        Signal signal = QuantitativeMonitor
                .atomicFormula((PopulationState sign)-> sign.getOccupancy(0) - 3.0).monitor(t);

        Signal signalParsed = parsedQuantitativeMonitor.monitor(t);

        assertEquals(signal.getStart(),signalParsed.getStart());
        assertEquals(signal.getEnd(),signalParsed.getEnd());

        assertEquals(signal.valueAt(0.0),signalParsed.valueAt(0.0));
        assertEquals(signal.valueAt(1.0),signalParsed.valueAt(1.0));
        assertEquals(signal.valueAt(2.0),signalParsed.valueAt(2.0));
        assertEquals(signal.valueAt(3.0),signalParsed.valueAt(3.0));
        assertEquals(signal.valueAt(4.0),signalParsed.valueAt(4.0));
        assertEquals(signal.valueAt(5.0),signalParsed.valueAt(5.0));
        assertEquals(signal.valueAt(6.0),signalParsed.valueAt(6.0));
        assertEquals(signal.valueAt(7.0),signalParsed.valueAt(7.0));

    }



    @Test
    public void testParsedNegation() throws StlModelGenerationException {
        String TEST_FORMULA = " " +
                "measure mes_1 \n" +
                "formula eventuallyFormula [] : !([mes_1 >= 0]) endformula";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));


        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitor =
                stlModelFactory.getQuantitativeMonitor("eventuallyFormula");

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        Signal signal = QuantitativeMonitor.negation(QuantitativeMonitor.atomicFormula((PopulationState sign)-> sign.getOccupancy(0))).monitor(t);

        Signal signalParsed = parsedQuantitativeMonitor.monitor(t);


        assertEquals(signal.getStart(),signalParsed.getStart());
        assertEquals(signal.getEnd(),signalParsed.getEnd());

        assertEquals(signal.valueAt(0.0),signalParsed.valueAt(0.0));
        assertEquals(signal.valueAt(1.0),signalParsed.valueAt(1.0));
        assertEquals(signal.valueAt(2.0),signalParsed.valueAt(2.0));
        assertEquals(signal.valueAt(3.0),signalParsed.valueAt(3.0));
        assertEquals(signal.valueAt(4.0),signalParsed.valueAt(4.0));
        assertEquals(signal.valueAt(5.0),signalParsed.valueAt(5.0));
        assertEquals(signal.valueAt(6.0),signalParsed.valueAt(6.0));
        assertEquals(signal.valueAt(7.0),signalParsed.valueAt(7.0));

    }



    @Test public void testConjunctionAndDisjunction() throws StlModelGenerationException {

        String TEST_FORMULA = "measure mes_1 measure mes_2 " +
                "formula conjunction_formula [] : [mes_1 >= 0] && [mes_2 >= 0] endformula " +
                "formula disjunction_formula [] : [mes_1 >= 0] || [mes_2 >= 0] endformula";


        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));


        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitorConjunction =
                stlModelFactory.getQuantitativeMonitor("conjunction_formula");

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitorDisjunction =
                stlModelFactory.getQuantitativeMonitor("disjunction_formula");

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 1, 1, 1},
                new int[]{2, 6, 1, 1, 1, 0, 0}
        );

        QuantitativeMonitor<PopulationState> leftAtomic = QuantitativeMonitor.atomicFormula((PopulationState s) -> s.getOccupancy(0));
        QuantitativeMonitor<PopulationState> rightAtomic = QuantitativeMonitor.atomicFormula((PopulationState s) -> s.getOccupancy(1));

        QuantitativeMonitor<PopulationState> conjunctionMonitor = QuantitativeMonitor.conjunction(leftAtomic,rightAtomic);
        QuantitativeMonitor<PopulationState> disjunctionMonitor = QuantitativeMonitor.disjunction(leftAtomic,rightAtomic);

        Signal conjunctionSignal = conjunctionMonitor.monitor(t);
        Signal disjunctionSignal = disjunctionMonitor.monitor(t);

        Signal conjunctionSignalParsed = parsedQuantitativeMonitorConjunction.monitor(t);
        Signal disjunctionSignalParsed = parsedQuantitativeMonitorDisjunction.monitor(t);

        double timeToCheck = 0.0;
        for (int i = 0; i < 14; i++) {
            assertEquals(conjunctionSignal.valueAt(timeToCheck),conjunctionSignalParsed.valueAt(timeToCheck));
            assertEquals(disjunctionSignal.valueAt(timeToCheck),disjunctionSignalParsed.valueAt(timeToCheck));
            timeToCheck += 0.5;
        }
    }


    @Test public void monitors() throws StlModelGenerationException {

        String TEST_FORMULA = "measure mes_1 measure mes_2 " +
                "formula conjunction_formula [a=0,b=3] : [mes_1 >= 0] && [mes_2 >= 0] endformula " +
                "formula disjunction_formula [b=5,a=1] : [mes_1 >= 0] || [mes_2 >= 0] endformula";


        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));


        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);
        Map<String, Map<String, ToDoubleFunction<Map<String, Double>>>> monitors = stlModelFactory.getMonitors();
        assertTrue(monitors.containsKey("conjunction_formula"));
        assertEquals(2, monitors.keySet().size());
        assertEquals(2, monitors.get("conjunction_formula").size());

        for (String key : monitors.keySet()) {
            String[] values = monitors.get(key).keySet().toArray(new String[0]);
            System.out.println(key);
            System.out.println(Arrays.toString(values));
        }
    }


    @Test
    public void testEventuallyWithParametrization() throws StlModelGenerationException {
        String TEST_FORMULA = """
                measure mes\s
                formula id_formula [a=0,b=3] : \\E [a,b][mes >= 2.0] endformula""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes", s -> s.getOccupancy(0));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);


        QuantitativeMonitor<PopulationState> parsedEventually =
                stlModelFactory.getQuantitativeMonitor("id_formula");

        QuantitativeMonitor<PopulationState> parsedEventuallyBetween0and3 =
                stlModelFactory.getQuantitativeMonitor("id_formula", Map.of("a",0.0,"b",3.0));


        QuantitativeMonitor<PopulationState> parsedEventuallyBetween3and6 =
                stlModelFactory.getQuantitativeMonitor("id_formula", Map.of("a",3.0,"b",6.0));

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 0, 0, 1, 1, 2, 3, 3}
        );

        QuantitativeMonitor<PopulationState> aM = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0) - 2.0);

        Interval interval1 = new Interval(0,3);
        Interval interval2 = new Interval(3,6);

        QuantitativeMonitor<PopulationState> eventuallyBetween0and3 = QuantitativeMonitor.eventually(interval1,aM);
        QuantitativeMonitor<PopulationState> eventuallyBetween3and6 = QuantitativeMonitor.eventually(interval2,aM);

        Signal se1 = eventuallyBetween0and3.monitor(t);
        Signal se2 = eventuallyBetween3and6.monitor(t);

        Signal seParsed = parsedEventually.monitor(t);
        Signal se1Parsed = parsedEventuallyBetween0and3.monitor(t);
        Signal se2Parsed = parsedEventuallyBetween3and6.monitor(t);
        System.out.println(seParsed);
        System.out.println(se1Parsed);
        System.out.println(se2Parsed);


        double timeToCheck = 0.0;
        for (int i = 0; i < 16; i++) {
            assertEquals(se1.valueAt(timeToCheck),seParsed.valueAt(timeToCheck));
            assertEquals(se1.valueAt(timeToCheck),se1Parsed.valueAt(timeToCheck));
            assertEquals(se2.valueAt(timeToCheck),se2Parsed.valueAt(timeToCheck));
            timeToCheck += 0.5;
        }
    }


    @Test
    public void testGloballyWithParametrization() throws StlModelGenerationException {
        String TEST_FORMULA = """
                 measure mes \s
                formula id_formula [a=0,b=3] : \\G [a,b][mes >= 2.0] endformula""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes", s -> s.getOccupancy(0));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedGloballyBetween0and3 =
                stlModelFactory.getQuantitativeMonitor("id_formula", Map.of("a",0.0,"b",3.0,"notAKey",100.0));
        QuantitativeMonitor<PopulationState> parsedGloballyBetween3and6 =
                stlModelFactory.getQuantitativeMonitor("id_formula", Map.of("a",3.0,"b",6.0));

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 0, 0, 1, 1, 2, 3, 3}
        );

        QuantitativeMonitor<PopulationState> aM = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0) - 2.0);

        Interval interval1 = new Interval(0,3);
        Interval interval2 = new Interval(3,6);

        QuantitativeMonitor<PopulationState> globallyBetween0and3 = QuantitativeMonitor.globally(interval1,aM);
        QuantitativeMonitor<PopulationState> globallyBetween3and6 = QuantitativeMonitor.globally(interval2,aM);

        Signal se1 = globallyBetween0and3.monitor(t);
        Signal se2 = globallyBetween3and6.monitor(t);

        Signal se1Parsed = parsedGloballyBetween0and3.monitor(t);
        Signal se2Parsed = parsedGloballyBetween3and6.monitor(t);

        double timeToCheck = 0.0;
        for (int i = 0; i < 16; i++) {
            assertEquals(se1.valueAt(timeToCheck),se1Parsed.valueAt(timeToCheck));
            assertEquals(se2.valueAt(timeToCheck),se2Parsed.valueAt(timeToCheck));
            timeToCheck += 0.5;
        }
    }

    @Test
    public void testUntil() throws StlModelGenerationException {
        String TEST_FORMULA = """
                 measure mes\s
                formula id_formula :[mes >= 2.0]  \\U [2,4][mes >= 4.0] endformula""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes", s -> s.getOccupancy(0));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedUntilMonitor =
                stlModelFactory.getQuantitativeMonitor("id_formula");
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 2.0, 1.0, 1.0, 3.0, 1.0},
                new int[]{0, 2, 3, 3, 3, 5, 1}
        );

        QuantitativeMonitor<PopulationState> aM1 = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0)- 2.0);
        QuantitativeMonitor<PopulationState> aM2 = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0)- 4.0);

        Signal sUntil = QuantitativeMonitor.until(aM1,new Interval(2,4),aM2).monitor(t);

        Signal sUntilParsed = parsedUntilMonitor.monitor(t);

        double timeToCheck = 0.0;
        for (int i = 0; i < 20; i++) {
            assertEquals(sUntil.valueAt(timeToCheck),sUntilParsed.valueAt(timeToCheck));
            timeToCheck += 0.5;
        }
    }

    @Test
    public void testNested() throws StlModelGenerationException {
        String TEST_FORMULA = """
                 measure mes\s
                formula id_formula [] : \\E[1,120](  [mes > 25]  && \\E[1,20][ mes > 25] ) endformula""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes", s -> s.getOccupancy(2));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedUntilMonitor =
                stlModelFactory.getQuantitativeMonitor("id_formula");


        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{11, 3, 28, 28, 40, 20, 16, 6, 6, 14, 77, 62, 49},
                new int[]{95,95,94,94,94,93,92,91,91,91,91,91,91,91},
                new int[]{ 5, 4, 5, 4, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0},
                new int[]{ 0, 1, 1, 2, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9}
        );


        QuantitativeMonitor<PopulationState> atomicMonitor =  QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(2) - 25);

        Signal s = QuantitativeMonitor
                .eventually(new Interval(1,120),QuantitativeMonitor
                        .conjunction(atomicMonitor,QuantitativeMonitor.eventually(new Interval(1,20),atomicMonitor))).monitor(t);


        Signal sParsed = parsedUntilMonitor.monitor(t);


        double timeToCheck = 0.0;
        for (int i = 0; i < 20; i++) {
            assertEquals(s.valueAt(timeToCheck),sParsed.valueAt(timeToCheck));
            timeToCheck += 0.5;
        }
    }

    @Test
    public void testParenthesis() throws StlModelGenerationException {
        String TEST_FORMULA = """
                 measure mes_1\s
                measure mes_2\s
                measure mes_3\s
                formula id_formula1 : [mes_1 > 0] || ( [mes_2 > 0] && [mes_3 > 0] ) endformula
                 formula id_formula2 [] : ( [mes_1 > 0] || [mes_2 > 0] ) && [mes_3 > 0] endformula
                \s""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));
        measure.put("mes_3", s -> s.getOccupancy(2));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> formula1 =
                stlModelFactory.getQuantitativeMonitor("id_formula1");

        QuantitativeMonitor<PopulationState> formula2 =
                stlModelFactory.getQuantitativeMonitor("id_formula2");

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{11, 3, 28, 28, 40, 20, 16, 6, 6, 14, 77, 62, 49},
                new int[]{95,95,94,94,94,93,92,91,91,91,91,91,91,91},
                new int[]{ 5, 4, 5, 4, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0},
                new int[]{ 0, 1, 1, 2, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9}
        );


        QuantitativeMonitor<PopulationState> atomicMonitor1 =  QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0));
        QuantitativeMonitor<PopulationState> atomicMonitor2 =  QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(1));
        QuantitativeMonitor<PopulationState> atomicMonitor3 =  QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(2));

        Signal s1 = QuantitativeMonitor.disjunction(atomicMonitor1,QuantitativeMonitor.conjunction(atomicMonitor2,atomicMonitor3)).monitor(t);
        Signal s2 = QuantitativeMonitor.conjunction(QuantitativeMonitor.disjunction(atomicMonitor1, atomicMonitor2), atomicMonitor3).monitor(t);

        Signal s1Parsed = formula1.monitor(t);
        Signal s2Parsed = formula2.monitor(t);

        assertEquals(s1.valueAt(0),s1Parsed.valueAt(0));
        assertEquals(s2.valueAt(0),s2Parsed.valueAt(0));
        assertEquals(s1.valueAt(180),s1Parsed.valueAt(180));
        assertEquals(s2.valueAt(180),s2Parsed.valueAt(180));
        assertEquals(s1.valueAt(360),s1Parsed.valueAt(360));
        assertEquals(s2.valueAt(360),s2Parsed.valueAt(360));
    }

    @Test
    public void testParsedAtomicFormulaQualitative() throws StlModelGenerationException {
        String TEST_FORMULA = """
                measure mes_1\s
                measure mes_2\s
                formula atomicF [] : [mes_1 > 3] endformula""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));

        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QualitativeMonitor<PopulationState> parsedQuantitativeMonitor =
                stlModelFactory.getQualitativeMonitor("atomicF");

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        BooleanSignal signal = QualitativeMonitor
                .atomicFormula((PopulationState sign)-> sign.getOccupancy(0) > 3).monitor(t);

        BooleanSignal signalParsed = parsedQuantitativeMonitor.monitor(t);

        assertEquals(signal.getValueAt(0.0),signalParsed.getValueAt(0.0));
        assertEquals(signal.getValueAt(1.0),signalParsed.getValueAt(1.0));
        assertEquals(signal.getValueAt(2.0),signalParsed.getValueAt(2.0));
        assertEquals(signal.getValueAt(3.0),signalParsed.getValueAt(3.0));
        assertEquals(signal.getValueAt(4.0),signalParsed.getValueAt(4.0));
        assertEquals(signal.getValueAt(5.0),signalParsed.getValueAt(5.0));
        assertEquals(signal.getValueAt(6.0),signalParsed.getValueAt(6.0));
        assertEquals(signal.getValueAt(7.0),signalParsed.getValueAt(7.0));
    }

    @Test
    public void testParsedQualitativeFormulaOnSir() throws StlModelGenerationException {
        String TEST_FORMULA =  """
                measure #I
                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
                """;

        // infection ends too soon   30  40  50  60  70  80  90 100 110 120
        Trajectory<PopulationState> trjNotSat1 = getPopulationTrajectory(
                new double[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
                new int[]{    5, 40, 20, 10,  5,  0,  0,  0,  0,  0,  0,  0 }
        );

        // infection never ends  20  30  40  50  60  70  80  90 100 110 120
        Trajectory<PopulationState> trjNotSat2 = getPopulationTrajectory(
                new double[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 },
                new int[]{    5, 40, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20 }
        );

        // satisfied         10  20  30  40  50  60  70  80  90 100 110 120
        Trajectory<PopulationState> trjSatisfy = getPopulationTrajectory(
                new double[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 },
                new int[]{    5, 40, 20, 20, 20, 20, 20, 20, 20, 20,  1,  0 }
        );

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("#I", s -> s.getOccupancy(0));

        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QualitativeMonitor<PopulationState> qualMonitor =
                stlModelFactory.getQualitativeMonitor("formula_id");


        BooleanSignal booleanSignal1 = qualMonitor.monitor(trjNotSat1);
        BooleanSignal booleanSignal2 = qualMonitor.monitor(trjNotSat2);
        BooleanSignal booleanSignal3 = qualMonitor.monitor(trjSatisfy);

        assertTrue(booleanSignal1.isEmpty());
        assertTrue(booleanSignal2.isEmpty());
        assertFalse(booleanSignal3.isEmpty());

    }

    @Test
    public void testParsedQuantitativeFormulaOnSir() throws StlModelGenerationException {
        String TEST_FORMULA =  """
                measure #I
                formula formula_id [] : ( \\E[100,120][ #I < 1] )&& (\\G[0,100][ #I > 0 ]) endformula
                """;


        // infection ends too soon   30  40  50  60  70  80  90 100 110 120
        Trajectory<PopulationState> trjNotSat1 = getPopulationTrajectory(
                new double[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
                new int[]{    5, 40, 20, 10,  5,  0,  0,  0,  0,  0,  0,  0 }
        );

        // infection never ends  20  30  40  50  60  70  80  90 100 110 120
        Trajectory<PopulationState> trjNotSat2 = getPopulationTrajectory(
                new double[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 },
                new int[]{    5, 40, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20 }
        );

        // satisfied         10  20  30  40  50  60  70  80  90 100 110 120
        Trajectory<PopulationState> trjSatisfy = getPopulationTrajectory(
                new double[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 },
                new int[]{    5, 40, 20, 20, 20, 20, 20, 20, 20, 20,  1,  0 }
        );

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("#I", s -> s.getOccupancy(0));

        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> quantitativeMonitor =
                stlModelFactory.getQuantitativeMonitor("formula_id");

        double robAtONotSat1 = quantitativeMonitor.monitor(trjNotSat1).valueAt(0.0);
        double robAtONotSat2 = quantitativeMonitor.monitor(trjNotSat2).valueAt(0.0);
        double robAt0Sat = quantitativeMonitor.monitor(trjSatisfy).valueAt(0.0);
        assertEquals(robAtONotSat1,0);
        assertEquals(robAtONotSat2,-19);
        assertEquals(robAt0Sat,1);
    }


    @Test
    public void testParsedAtomicFormulaWithLogicInside() throws StlModelGenerationException {

        String TEST_FORMULA = " measure mes_1 formula atomicF [] : [mes_1 > 3] && [mes_1 < 4] endformula";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));


        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitor =
                stlModelFactory.getQuantitativeMonitor("atomicF");

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        Signal signalParsed = parsedQuantitativeMonitor.monitor(t);

        QuantitativeMonitor<PopulationState> conjunctionMonitor = QuantitativeMonitor.conjunction(
                QuantitativeMonitor.atomicFormula((PopulationState sign)-> sign.getOccupancy(0) - 3.0),
                QuantitativeMonitor.atomicFormula((PopulationState sign)-> 4.0-sign.getOccupancy(0)));
        Signal signal = conjunctionMonitor.monitor(t);


        assertEquals(signal.getStart(),signalParsed.getStart());
        assertEquals(signal.getEnd(),signalParsed.getEnd());

        assertEquals(signal.valueAt(0.0),signalParsed.valueAt(0.0));
        assertEquals(signal.valueAt(1.0),signalParsed.valueAt(1.0));
        assertEquals(signal.valueAt(2.0),signalParsed.valueAt(2.0));
        assertEquals(signal.valueAt(3.0),signalParsed.valueAt(3.0));
        assertEquals(signal.valueAt(4.0),signalParsed.valueAt(4.0));
        assertEquals(signal.valueAt(5.0),signalParsed.valueAt(5.0));
        assertEquals(signal.valueAt(6.0),signalParsed.valueAt(6.0));
        assertEquals(signal.valueAt(7.0),signalParsed.valueAt(7.0));

    }

    @SuppressWarnings("unused")
    @Disabled
    @Test
    void testParseError() throws StlModelGenerationException {
        String TEST_FORMULA = """
                measure R
                measure B
                measure M
                formula formula_stability [t=25] : \\E[0,t][ R >= 1.0 ]   endformula
                formula formula_coherence [t=25] : ([ R > 3 * B ]&&(\\E[0,t][ B <= 0.0])) endformula
                formula formula_red_pres [t=25] : (\\G[0,t][ R >= 0.5]) endformula
                formula formula_mal_pres [t=25] : ((\\E[0, 1/4 * t]([M >= 0.01]))<->(\\G[3/4 * t,t]([M >= 0.25] && [M <= 0.75]))) endformula
                """;
        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("R", s -> s.getOccupancy(0));
        measure.put("B", s -> s.getOccupancy(1));
        measure.put("M", s -> s.getOccupancy(2));


        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

    }


    /**
     * 0.0:[90, 10, 0] --> 0.39431566657167305:[90, 9, 1] --> 0.5053485249608954:[89, 10, 1] --> 1.044933361765899:[89, 9, 2] --> 2.174221015809152:[89, 8, 3] --> 2.8797601145787106:[88, 9, 3] --> 3.225023155754782:[88, 8, 4] --> 3.289675513149529:[87, 9, 4] --> 3.702892944707358:[86, 10, 4] --> 4.31994889800267:[85, 11, 4] --> 4.506332014243528:[85, 10, 5] --> 4.5724601649349506:[85, 9, 6] --> 6.205083965049217:[84, 10, 6] --> 7.279092670894575:[84, 9, 7] --> 7.658599509593253:[84, 8, 8] --> 10.166814252998206:[84, 7, 9] --> 10.797159693176154:[83, 8, 9] --> 11.33442988833734:[82, 9, 9] --> 11.697942230599075:[81, 10, 9] --> 13.369230472552958:[80, 11, 9] --> 13.500347038084332:[79, 12, 9] --> 13.829706786620442:[78, 13, 9] --> 14.0872852085095:[77, 14, 9] --> 14.118368361007837:[76, 15, 9] --> 14.769705307886413:[75, 16, 9] --> 15.29096211671821:[75, 15, 10] --> 15.319138397640407:[74, 16, 10] --> 15.533500101512615:[74, 15, 11] --> 16.19033430764179:[73, 16, 11] --> 16.392316646850553:[72, 17, 11] --> 16.555801407784923:[71, 18, 11] --> 16.56646387568298:[71, 17, 12] --> 16.775922550850506:[70, 18, 12] --> 18.54458706689295:[69, 19, 12] --> 18.58522180649093:[68, 20, 12] --> 18.848287919694673:[67, 21, 12] --> 18.91262637637121:[66, 22, 12] --> 18.95003924598895:[65, 23, 12] --> 19.048779750648247:[64, 24, 12] --> 19.791694494283746:[63, 25, 12] --> 20.430428274368015:[63, 24, 13] --> 20.437975687295864:[62, 25, 13] --> 20.79454984498183:[61, 26, 13] --> 21.109627572485426:[60, 27, 13] --> 21.221834683284722:[59, 28, 13] --> 21.581976338620873:[58, 29, 13] --> 21.607563484977877:[57, 30, 13] --> 21.618873485446645:[56, 31, 13] --> 21.81231410802491:[55, 32, 13] --> 21.998792461545086:[54, 33, 13] --> 22.041562553173492:[53, 34, 13] --> 22.086760057898843:[53, 33, 14] --> 22.125847953170993:[52, 34, 14] --> 22.195704899309575:[51, 35, 14] --> 22.36574620344519:[51, 34, 15] --> 23.091194940433102:[51, 33, 16] --> 23.232515777471985:[50, 34, 16] --> 23.53038647564842:[49, 35, 16]
     * @throws StlModelGenerationException
     */
    @Disabled
    @Test
    public void testProblematicCase() throws StlModelGenerationException {
        String TEST_FORMULA = """
                measure %I
                formula formula_with_id [a=0,b=100] : ( \\G[a,(a+b)][ %I > 0.3] && \\G[(a+b),(a+b+b)][ %I < 0.3] ) endformula
                formula formula_with_id_reduced [a=0,b=100] : ( \\G[(a+b),(a+b+b)][ %I < 0.3] ) endformula
                formula formula_id [a=0,b=100] : ( \\G[4.125792580104338,4.125792580104338+5.092318988523647][ %I > 0.3] && \\G[4.125792580104338+5.092318988523647,4.125792580104338+5.092318988523647+5.092318988523647][ %I < 0.3] ) endformula
                formula formula_id_summed [a=0,b=100] : ( \\G[4.125792580104338,9.218111568627985][ %I > 0.3] && \\G[9.218111568627985,14.310430557151632][ %I < 0.3] ) endformula
                formula formula_id_approx [a=0,b=100] : ( \\G[4.13,4.13+5.09][ %I > 0.3] && \\G[4.13+5.09,4.123+5.09+5.09][ %I < 0.3] ) endformula
                """;

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("%S", s -> s.getOccupancy(0));
        measure.put("%I", s -> s.getOccupancy(1));
        measure.put("%R", s -> s.getOccupancy(2));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);


        Trajectory<PopulationState> t = getPopulationTrajectoryAtTimes(
                new double[]{
                        0.0,                  // First point
                        21.581976338620873,   // Middle point 2
                        23.53038647564842     // Last point
                },
                new int[]{90,58, 49},  // S values
                new int[]{10,  29, 35},  // I values
                new int[]{ 0, 13, 16}   // R values
        );

        //QuantitativeMonitor<PopulationState> quantitativeMonitor = stlModelFactory.getQuantitativeMonitor("formula_id", Map.of("a", 0, "b", 100));

        QuantitativeMonitor<PopulationState> quantitativeMonitor = stlModelFactory.getQuantitativeMonitor("formula_with_id_reduced", Map.of("a", 4.125792580104338, "b", 5.092318988523647));

        //QuantitativeMonitor<PopulationState> quantitativeMonitor = stlModelFactory.getQuantitativeMonitor("formula_with_id_reduced", Map.of("a", 4.126, "b", 5.092));

        double robustnessAt0 = quantitativeMonitor.monitor(t).valueAt(0);
        System.out.println(robustnessAt0);

    }


    /**
     * 0.0:[90, 10, 0] --> 0.39431566657167305:[90, 9, 1] --> 0.5053485249608954:[89, 10, 1] --> 1.044933361765899:[89, 9, 2] --> 2.174221015809152:[89, 8, 3] --> 2.8797601145787106:[88, 9, 3] --> 3.225023155754782:[88, 8, 4] --> 3.289675513149529:[87, 9, 4] --> 3.702892944707358:[86, 10, 4] --> 4.31994889800267:[85, 11, 4] --> 4.506332014243528:[85, 10, 5] --> 4.5724601649349506:[85, 9, 6] --> 6.205083965049217:[84, 10, 6] --> 7.279092670894575:[84, 9, 7] --> 7.658599509593253:[84, 8, 8] --> 10.166814252998206:[84, 7, 9] --> 10.797159693176154:[83, 8, 9] --> 11.33442988833734:[82, 9, 9] --> 11.697942230599075:[81, 10, 9] --> 13.369230472552958:[80, 11, 9] --> 13.500347038084332:[79, 12, 9] --> 13.829706786620442:[78, 13, 9] --> 14.0872852085095:[77, 14, 9] --> 14.118368361007837:[76, 15, 9] --> 14.769705307886413:[75, 16, 9] --> 15.29096211671821:[75, 15, 10] --> 15.319138397640407:[74, 16, 10] --> 15.533500101512615:[74, 15, 11] --> 16.19033430764179:[73, 16, 11] --> 16.392316646850553:[72, 17, 11] --> 16.555801407784923:[71, 18, 11] --> 16.56646387568298:[71, 17, 12] --> 16.775922550850506:[70, 18, 12] --> 18.54458706689295:[69, 19, 12] --> 18.58522180649093:[68, 20, 12] --> 18.848287919694673:[67, 21, 12] --> 18.91262637637121:[66, 22, 12] --> 18.95003924598895:[65, 23, 12] --> 19.048779750648247:[64, 24, 12] --> 19.791694494283746:[63, 25, 12] --> 20.430428274368015:[63, 24, 13] --> 20.437975687295864:[62, 25, 13] --> 20.79454984498183:[61, 26, 13] --> 21.109627572485426:[60, 27, 13] --> 21.221834683284722:[59, 28, 13] --> 21.581976338620873:[58, 29, 13] --> 21.607563484977877:[57, 30, 13] --> 21.618873485446645:[56, 31, 13] --> 21.81231410802491:[55, 32, 13] --> 21.998792461545086:[54, 33, 13] --> 22.041562553173492:[53, 34, 13] --> 22.086760057898843:[53, 33, 14] --> 22.125847953170993:[52, 34, 14] --> 22.195704899309575:[51, 35, 14] --> 22.36574620344519:[51, 34, 15] --> 23.091194940433102:[51, 33, 16] --> 23.232515777471985:[50, 34, 16] --> 23.53038647564842:[49, 35, 16]
     * @throws StlModelGenerationException
     */
    @Disabled
    @Test
    public void testProblematicCaseA() throws StlModelGenerationException {
        String TEST_FORMULA = """
                measure %I
                formula formula_with_id [a=0,b=100] : ( \\G[a,(a+b)][ %I > 0.3] && \\G[(a+b),(a+b+b)][ %I < 0.3] ) endformula
                formula formula_with_id_reduced [a=0,b=100] : ( \\G[(a+b),(a+b+b)][ %I < 0.3] ) endformula
                formula formula_id [a=0,b=100] : ( \\G[4.125792580104338,4.125792580104338+5.092318988523647][ %I > 0.3] && \\G[4.125792580104338+5.092318988523647,4.125792580104338+5.092318988523647+5.092318988523647][ %I < 0.3] ) endformula
                formula formula_id_summed [a=0,b=100] : ( \\G[4.125792580104338,9.218111568627985][ %I > 0.3] && \\G[9.218111568627985,14.310430557151632][ %I < 0.3] ) endformula
                formula formula_id_approx [a=0,b=100] : ( \\G[4.13,4.13+5.09][ %I > 0.3] && \\G[4.13+5.09,4.123+5.09+5.09][ %I < 0.3] ) endformula
                """;

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("%S", s -> s.getOccupancy(0));
        measure.put("%I", s -> s.getOccupancy(1));
        measure.put("%R", s -> s.getOccupancy(2));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        Trajectory<PopulationState> t = getPopulationTrajectoryAtTimes(
                new double[]{
                        0.0, 0.39431566657167305, 0.5053485249608954, 1.044933361765899, 2.174221015809152,
                        2.8797601145787106, 3.225023155754782, 3.289675513149529, 3.702892944707358,
                        4.31994889800267, 4.506332014243528, 4.5724601649349506, 6.205083965049217,
                        7.279092670894575, 7.658599509593253, 10.166814252998206, 10.797159693176154,
                        11.33442988833734, 11.697942230599075, 13.369230472552958, 13.500347038084332,
                        13.829706786620442, 14.0872852085095, 14.118368361007837, 14.769705307886413,
                        15.29096211671821, 15.319138397640407, 15.533500101512615, 16.19033430764179,
                        16.392316646850553, 16.555801407784923, 16.56646387568298, 16.775922550850506,
                        18.54458706689295, 18.58522180649093, 18.848287919694673, 18.91262637637121,
                        18.95003924598895, 19.048779750648247, 19.791694494283746, 20.430428274368015,
                        20.437975687295864, 20.79454984498183, 21.109627572485426, 21.221834683284722,
                        21.581976338620873, 21.607563484977877, 21.618873485446645, 21.81231410802491,
                        21.998792461545086, 22.041562553173492, 22.086760057898843, 22.125847953170993,
                        22.195704899309575, 22.36574620344519, 23.091194940433102, 23.232515777471985,
                        23.53038647564842
                },
                new int[]{90,90,89,89,89,88,88,87,86,85,85,85,84,84,84,84,83,82,81,80,79,78,77,76,75,75,74,74,73,72,71,71,70,69,68,67,66,65,64,63,63,62,61,60,59,58,57,56,55,54,53,53,52,51,51,51,50,49},
                new int[]{10, 9,10, 9, 8, 9, 8, 9,10,11,10, 9,10, 9, 8, 7, 8, 9,10,11,12,13,14,15,16,15,16,15,16,17,18,17,18,19,20,21,22,23,24,25,24,25,26,27,28,29,30,31,32,33,34,33,34,35,34,33,34,35},
                new int[]{ 0, 1, 1, 2, 3, 3, 4, 4, 4, 4, 5, 6, 6, 7, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,10,10,11,11,11,11,12,12,12,12,12,12,12,12,12,13,13,13,13,13,13,13,13,13,13,13,14,14,14,15,16,16,16}
        );

        //QuantitativeMonitor<PopulationState> quantitativeMonitor = stlModelFactory.getQuantitativeMonitor("formula_id", Map.of("a", 0, "b", 100));

        QuantitativeMonitor<PopulationState> quantitativeMonitor = stlModelFactory.getQuantitativeMonitor("formula_with_id_reduced", Map.of("a", 4.125792580104338, "b", 5.092318988523647));


        double robustnessAt0 = quantitativeMonitor.monitor(t).valueAt(0);
        System.out.println(robustnessAt0);

    }


}
