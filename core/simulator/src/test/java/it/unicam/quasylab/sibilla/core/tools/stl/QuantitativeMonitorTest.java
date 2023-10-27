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

        trajectory.setEnd(time);

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

        assertEquals(s.valueAt(0.5),-0.0);
        assertEquals(s.valueAt(1.5),-8.0);
    }



    /**
     *
     *   index 0
     *   8 |
     *   7 |
     *   6 |
     *   5 |
     *   4 |
     *   3 |                        XXX
     *   2 |
     *   1 |            XXX XXX XXX
     *   0  XXX XXX XXX --- --- --- ---
     *       1   2   3   4   5   6   7
     * TEST
     *    ( X >= 3 )
     * TRAJECTORY:
     *    X = [(0, 0)(1, 0),(2, 0),(3, 1),(4, 1),(5, 1),(6, 3)]
     * ROBUSTNESS:
     *    r = [(0,-3)(1, 5),(2, 0),(3,-1),(4,-2),(5,-2),(6,-2)]
     */
    @Test
    public void testEventuallyWithTwoDifferentInterval(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 3.0, 3.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> biggerThan2Monitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0) - 2.0);

        QuantitativeMonitor<PopulationState> eventuallyBetween0and3 = QuantitativeMonitor.eventually(biggerThan2Monitor,0,3);
        QuantitativeMonitor<PopulationState> eventuallyBetween4and6 = QuantitativeMonitor.eventually(biggerThan2Monitor,3,6);

        Signal se1 = eventuallyBetween0and3.monitor(t);
        Signal se2 = eventuallyBetween4and6.monitor(t);

        assertTrue(se1.valueAt(0)<se2.valueAt(0) );
        assertTrue(se1.getEnd() > se2.getEnd());
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


        Signal s = QuantitativeMonitor.globally(atomicMonitor,1,4).monitor(t);

        assertEquals(s.valueAt(0.0),0.0);
        assertTrue(
                QuantitativeMonitor.globally(atomicMonitor,0,3).monitor(t).valueAt(0)
                        <
                        QuantitativeMonitor.globally(atomicMonitor,1,4).monitor(t).valueAt(0)
        );
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
     *   1 |                XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX
     *   0  XXX --- --- --- --- --- --- --- --- --- --- --- --- --- ---
     *       1   2   3   4   5   6   7   8   9   10  11  12  13  14  15
     * TEST
     *    ( X >= 6 )
     */
    @Test
    public void testEventually(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 9.0, 14.0},
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0)- 6.0);

        Signal s = QuantitativeMonitor.eventually(atomicMonitor,0,4).monitor(t);

        assertEquals(s.valueAt(0.0),2.0);

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
                new double[]{1.0, 1.0, 2.0, 1.0, 1.0, 3.0, 1.0},
                1,
                new double[]{0.0, 2.0, 3.0, 3.0, 3.0, 5.0, 1.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> aM1 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0) - 2.0);

        QuantitativeMonitor.AtomicMonitor<PopulationState> aM2 =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0)- 4.0);

        Signal s = QuantitativeMonitor.until(aM1,2,4,aM2).monitor(t);


        assertEquals(s.getEnd(), 6.0);
        assertEquals(s.valueAt(0.0),-2.0);

    }


    @Test
    public void testEventuallyTimeShift(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{1.0, 1.0, 1.0, 1.0, 3.0, 4.0, 3.0, 3.0}
        );

        QuantitativeMonitor.AtomicMonitor<PopulationState> aM =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(0) - 2.0);

        QuantitativeMonitor<PopulationState> eventuallyFrom0to3 = QuantitativeMonitor.eventually(aM,0.0,3.0);
        QuantitativeMonitor<PopulationState> eventuallyFrom3to5 = QuantitativeMonitor.eventually(aM,3.0,5.0);

        Signal sFrom0To3 = eventuallyFrom0to3.monitor(t);
        Signal sFrom3To5 = eventuallyFrom3to5.monitor(t);

        assertEquals(sFrom0To3.getEnd(),5.0);
        assertEquals(sFrom3To5.getEnd(),3.0);
        assertEquals(sFrom0To3.valueAt(0),-1.0);
        assertEquals(sFrom3To5.valueAt(0),2.0);

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
        assertEquals(s.valueAt(0.0),-2.0);


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


        QuantitativeMonitor.AtomicMonitor<PopulationState> atomicMonitor =
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(2) - 25);

        Signal s = QuantitativeMonitor.eventually(atomicMonitor,0,500.0).monitor(t);

        assertEquals(s.valueAt(0.0),-17.0);

    }



}