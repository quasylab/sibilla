package it.unicam.quasylab.sibilla.langs.stl;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.core.util.Signal;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

public class TestStlParser {


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

        String TEST_FORMULA = " measure mes_1 measure mes_2 formula atomicF [] : [mes_1 > 3] endformula";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);

        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));


        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitor =
                stlModelFactory.getQuantitativeMonitor("atomicF", new double[]{});

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
                stlModelFactory.getQuantitativeMonitor("eventuallyFormula", new double[]{});

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
                stlModelFactory.getQuantitativeMonitor("conjunction_formula", new double[]{});

        QuantitativeMonitor<PopulationState> parsedQuantitativeMonitorDisjunction =
                stlModelFactory.getQuantitativeMonitor("disjunction_formula", new double[]{});

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

    @Test
    public void testEventuallyWithParametrization() throws StlModelGenerationException {
        String TEST_FORMULA = """
                 measure mes\s
                formula id_formula [a=0,b=3] : \\E [a,b][mes >= 2.0] endformula""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes", s -> s.getOccupancy(0));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedEventuallyBetween0and3 =
                stlModelFactory.getQuantitativeMonitor("id_formula", new double[]{0.0,3.0});
        QuantitativeMonitor<PopulationState> parsedEventuallyBetween3and6 =
                stlModelFactory.getQuantitativeMonitor("id_formula", new double[]{3.0,6.0});

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

        Signal se1Parsed = parsedEventuallyBetween0and3.monitor(t);
        Signal se2Parsed = parsedEventuallyBetween3and6.monitor(t);


        double timeToCheck = 0.0;
        for (int i = 0; i < 16; i++) {
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
                stlModelFactory.getQuantitativeMonitor("id_formula", new double[]{0.0,3.0});
        QuantitativeMonitor<PopulationState> parsedGloballyBetween3and6 =
                stlModelFactory.getQuantitativeMonitor("id_formula", new double[]{3.0,6.0});

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
                formula id_formula [] :[mes >= 2.0]  \\U [2,4][mes >= 4.0] endformula""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes", s -> s.getOccupancy(0));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> parsedUntilMonitor =
                stlModelFactory.getQuantitativeMonitor("id_formula", new double[]{});
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
                stlModelFactory.getQuantitativeMonitor("id_formula", new double[]{});


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
                formula id_formula1 [] : [mes_1 > 0] || ( [mes_2 > 0] && [mes_3 > 0] ) endformula
                 formula id_formula2 [] : ( [mes_1 > 0] || [mes_2 > 0] ) && [mes_3 > 0] endformula
                \s""";

        StlLoader stlLoader = new StlLoader(TEST_FORMULA);
        Map<String, ToDoubleFunction<PopulationState>> measure = new HashMap<>();
        measure.put("mes_1", s -> s.getOccupancy(0));
        measure.put("mes_2", s -> s.getOccupancy(1));
        measure.put("mes_3", s -> s.getOccupancy(2));
        StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(measure);

        QuantitativeMonitor<PopulationState> formula1 =
                stlModelFactory.getQuantitativeMonitor("id_formula1", new double[]{});

        QuantitativeMonitor<PopulationState> formula2 =
                stlModelFactory.getQuantitativeMonitor("id_formula2", new double[]{});

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
                stlModelFactory.getQualitativeMonitor("atomicF", new double[]{});

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
                stlModelFactory.getQualitativeMonitor("formula_id", new double[]{});


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
                stlModelFactory.getQuantitativeMonitor("formula_id", new double[]{});

        double robAtONotSat1 = quantitativeMonitor.monitor(trjNotSat1).valueAt(0.0);
        double robAtONotSat2 = quantitativeMonitor.monitor(trjNotSat2).valueAt(0.0);
        double robAt0Sat = quantitativeMonitor.monitor(trjSatisfy).valueAt(0.0);
        assertEquals(robAtONotSat1,0);
        assertEquals(robAtONotSat2,-19);
        assertEquals(robAt0Sat,1);
    }

}
