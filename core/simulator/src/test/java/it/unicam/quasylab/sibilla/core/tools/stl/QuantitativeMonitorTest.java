package it.unicam.quasylab.sibilla.core.tools.stl;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.Signal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuantitativeMonitorTest {

    private Trajectory<PopulationState> getPopulationTrajectory(double[] timeIntervals, int numPopulations, double[]... signals) {
        Trajectory<PopulationState> trajectory = new Trajectory<>();
        double time = 0.0;

        for (int i = 0; i < timeIntervals.length; i++) {
            double currentTimeInterval = timeIntervals[i];
            Population[] populations = new Population[numPopulations];

            for (int j = 0; j < numPopulations; j++) {
                populations[j] = new Population(j, (int) signals[j][i]);
            }

            trajectory.add(time, new PopulationState(numPopulations, populations));
            time += currentTimeInterval;
        }

        return trajectory;
    }



    /**
     *
     *   index 0
     *   8 |    XXX
     *   7 |
     *   6 |
     *   5 |
     *   4 |
     *   3 |        XXX
     *   2 |            XXX
     *   1 |                XXX XXX XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     * TEST
     *    ( X >= 3 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 8),(2, 3),(3, 2),(4, 1),(5, 1),(6, 1)]
     * ROBUSTNESS:
     *    r = [(0,-3)(1, 5),(2, 0),(3,-1),(4,-2),(5,-2),(6,-2)]
     */
    @Test
    public void testAtomicFormula(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0) - 3.0);

        Signal s = atomicMonitor.monitor(t);
        assertEquals(s.valueAt(0.5),-3.0);
        assertEquals(s.valueAt(1.5),5.0);
        assertEquals(s.valueAt(2.5),0.0);
        assertEquals(s.valueAt(3.5),-1.0);
        assertEquals(s.valueAt(1.0),5.0);
        assertEquals(s.valueAt(2.0),0.0);


    }





    /**
     *
     *   index 0
     *   8 |    XXX
     *   7 |
     *   6 |
     *   5 |
     *   4 |
     *   3 |        XXX
     *   2 |            XXX
     *   1 |                XXX XXX XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     * TEST
     *    ( X >= 3 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 8),(2, 3),(3, 2),(4, 1),(5, 1),(6, 1)]
     * ROBUSTNESS:
     *    r = [(0,-0)(1,-8),(2,-3),(3,-2),(4,-1),(5,-1),(6,-1)]
     */
    @Test
    public void testNegationAtomicFormula(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0));


        Signal s = QuantitativeMonitor.negation(atomicMonitor).monitor(t);

        System.out.println(s);

        assertEquals(s.valueAt(0.5),-0.0);
        assertEquals(s.valueAt(1.5),-8.0);
    }


    /**
     *
     *   index 0
     *   8 |    XXX
     *   7 |
     *   6 |
     *   5 |
     *   4 |
     *   3 |        XXX
     *   2 |            XXX
     *   1 |                XXX XXX XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     * TEST
     *    ( X >= 3 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 8),(2, 3),(3, 2),(4, 1),(5, 1),(6, 1)]
     * ROBUSTNESS:
     *    r = [(0,-0)(1,-8),(2,-3),(3,-2),(4,-1),(5,-1),(6,-1)]
     */
    @Test
    public void testGlobally(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0)- 1.0);


        Signal s = QuantitativeMonitor.globally(atomicMonitor,0,1).monitor(t);

        System.out.println(s);

        //assertEquals(s.valueAt(0.5),-0.0);
        //assertEquals(s.valueAt(1.5),-8.0);
    }



    /**
     *
     *   index 0
     *   8 |
     *   7 |
     *   6 |    XXX
     *   5 |
     *   4 |
     *   3 |        XXX
     *   2 |            XXX
     *   1 |                XXX XXX XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     * TEST
     *    ( X >= 6 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 8),(2, 3),(3, 2),(4, 1),(5, 1),(6, 1)]
     * ROBUSTNESS:
     *    r = [(0, 2)(1, 2),(2,-3),(3,-3),(4,-3),(5,-3),(6,-3)]
     */
    @Test
    public void testEventually(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0)- 6.0);


        Signal s = QuantitativeMonitor.eventually(atomicMonitor,0,4).monitor(t);

        System.out.println(s);

        assertEquals(s.valueAt(0.0),2.0);

        //assertEquals(s.valueAt(0.5),-0.0);
        //assertEquals(s.valueAt(1.5),-8.0);
    }


    /**
     *
     *   index 0
     *   8 |
     *   7 |
     *   6 |
     *   5 |                    XXX
     *   4 |
     *   3 |        XXX
     *   2 |    XXX     XXX         XXX
     *   1 |                XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     * TEST
     *    ( X >= 2 U[0,1] X >= 4 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 2),(2, 3),(3, 2),(4, 1),(5, 5),(6, 2)]
     * ROBUSTNESS:
     *    r = [(0, 2)(1, 2),(2,-3),(3,-3),(4,-3),(5,-3),(6,-3)]
     */
    @Test
    public void testUntil(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 2.0, 3.0, 2.0, 1.0, 5.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> aM1 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0) - 2.0);

        QuantitativeMonitor.AtomicMonitor<PopulationState> aM2 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0)- 4.0);


        Signal s = QuantitativeMonitor.until(aM1,0,2,aM2).monitor(t);

        System.out.println(s);

        //assertEquals(s.valueAt(0.0),2.0);

        //assertEquals(s.valueAt(0.5),-0.0);
        //assertEquals(s.valueAt(1.5),-8.0);
    }



    /**
     *
     *   index 0
     *   8 |
     *   7 |
     *   6 |
     *   5 |
     *   4 |    XXX
     *   3 |        XXX
     *   2 |            XXX
     *   1 |                XXX XXX XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     * TEST
     *    ( X >= 6 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 4),(2, 3),(3, 2),(4, 1),(5, 1),(6, 1)]
     * ROBUSTNESS:
     *    r = [(0,-2)(1,-2),(2,-3),(3,-3),(4,-3),(5,-3),(6,-3)]
     */
    @Test
    public void testEventuallyNotSatisfied(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 4.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0)- 6.0);


        Signal s = QuantitativeMonitor.eventually(atomicMonitor,0,4).monitor(t);

        System.out.println(s);

        assertEquals(s.valueAt(0.0),-2.0);

        //assertEquals(s.valueAt(0.5),-0.0);
        //assertEquals(s.valueAt(1.5),-8.0);
    }


    @Test
    public void testEventuallySIR(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{11, 3, 28, 28, 40, 20, 16, 6, 6, 14, 77, 62, 49},
                3,
                new double[]{95,95,94,94,94,93,92,91,91,91,91,91,91,91},
                new double[]{ 5, 4, 5, 4, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0},
                new double[]{ 0, 1, 1, 2, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9}
        );
        t.setEnd(1000);


        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(2) - 25);

        Signal aS = new QuantitativeMonitor.AtomicMonitor<PopulationState>(s -> s.getOccupancy(2) - 25).monitor(t);


        Signal s = QuantitativeMonitor.eventually(atomicMonitor,0,500.0).monitor(t);

        System.out.println(s);

        assertEquals(s.valueAt(0.0),-17.0);

        //assertEquals(s.valueAt(0.5),-0.0);
        //assertEquals(s.valueAt(1.5),-8.0);
    }

    /**
     *
     *   index 0
     *   8 |    XXX
     *   7 |
     *   6 |
     *   5 |
     *   4 |
     *   3 |        XXX
     *   2 |            XXX
     *   1 |                XXX XXX XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     *   index 1
     *   8 |
     *   7 |
     *   6 |    XXX
     *   5 |
     *   4 |
     *   3 |
     *   2 |XXX
     *   1 |        XXX XXX XXX
     *   0  --- --- --- --- --- XXX XXX
     *       1   2   3   4   5   6   7
     * TEST
     *    E_[0,1]( X >= 2 ) && E_[0,1]( Y >= 2 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 8),(2, 3),(3, 2),(4, 1),(5, 1),(6, 1)]
     *    Y = [(0, 2)(1, 6),(2, 1),(3, 1),(4, 1),(5, 0),(6, 0)]
     * ROBUSTNESS:
     *    r = [(0, 4)(1, 4),(2,-1),(3,-1),(4,-1),(5,-1),(6,-2)]
     */
    @Test
    public void test_formula_1(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                2,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0},
                new double[]{2.0, 6.0, 1.0, 1.0, 1.0, 0.0, 0.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor_1 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0) - 2.0);

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor_2 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(1) - 2.0);

        QuantitativeMonitor<PopulationState> eventually_1 =
                QuantitativeMonitor.eventually(atomicMonitor_1, 0, 1.0);
        QuantitativeMonitor<PopulationState> eventually_2 =
                QuantitativeMonitor.eventually(atomicMonitor_2, 0, 1.0);

        Signal s_e1 = eventually_1.monitor(t);
        Signal s_e2 = eventually_2.monitor(t);

        double[] element = s_e1.valuesAt(new double[]{5.0});
        System.out.println(element[0]);
        System.out.println(s_e1);
        System.out.println(s_e2);


        Signal s = QuantitativeMonitor.conjunction(eventually_1,eventually_2).monitor(t);


        //Signal s = QuantitativeMonitor.disjunction(atomicMonitor_1,atomicMonitor_2).monitor(t);

        //Signal s = atomicMonitor_1.monitor(t);

        System.out.println(s);

        Signal sCorrect = new Signal(new double[]{0.0,2.0,5.0},new double[]{4.0,-1.0,-2.0});

        assertEquals(s,sCorrect);

    }


    /**
     *   index 0
     *   8 |    XXX
     *   7 |
     *   6 |
     *   5 |
     *   4 |
     *   3 |        XXX
     *   2 |            XXX
     *   1 |                XXX XXX XXX
     *   0  XXX --- --- --- --- --- ---
     *       1   2   3   4   5   6   7
     *   index 1
     *   8 |
     *   7 |
     *   6 |    XXX
     *   5 |
     *   4 |
     *   3 |
     *   2 |XXX
     *   1 |        XXX XXX XXX     XXX
     *   0  --- --- --- --- --- XXX ---
     *       1   2   3   4   5   6   7
     * TEST
     *    E_[0,1]( X >= 2 ) && E_[0,1]( Y >= 2 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 8),(2, 3),(3, 2),(4, 1),(5, 1),(6, 1)]
     *    Y = [(0, 2)(1, 6),(2, 1),(3, 1),(4, 1),(5, 0),(6, 1)]
     * ROBUSTNESS:
     *    r = [(0, 4)(1, 4),(2,-1),(3,-1),(4,-1),(5,-1),(6,-2)]
     */
    @Test
    public void test_formula_2(){
        //Trajectory<PopulationState> t = getTestTrajectory_1();
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                2,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0},
                new double[]{2.0, 6.0, 1.0, 1.0, 1.0, 0.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor_1 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0) - 2.0);

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor_2 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(1) - 2.0);

        QuantitativeMonitor<PopulationState> eventually_1 =
                QuantitativeMonitor.eventually(atomicMonitor_1, 0, 1.0);
        QuantitativeMonitor<PopulationState> eventually_2 =
                QuantitativeMonitor.eventually(atomicMonitor_2, 0, 1.0);

        Signal s_e1 = eventually_1.monitor(t);
        Signal s_e2 = eventually_2.monitor(t);


        System.out.println(s_e1);
        System.out.println(s_e2);


        Signal s = QuantitativeMonitor.conjunction(eventually_1,eventually_2).monitor(t);


        //Signal s = QuantitativeMonitor.disjunction(atomicMonitor_1,atomicMonitor_2).monitor(t);

        //Signal s = atomicMonitor_1.monitor(t);

        double[] element = s.valuesAt(new double[]{5.0});
        System.out.println(element[0]);


        Signal sCorrect = new Signal(new double[]{0.0,2.0},new double[]{4.0,-1.0});

        assertEquals(-1.0,s.valueAt(5.0));
        assertEquals(s,sCorrect);


    }


}