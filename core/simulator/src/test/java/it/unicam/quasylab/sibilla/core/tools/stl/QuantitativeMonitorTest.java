package it.unicam.quasylab.sibilla.core.tools.stl;


import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.core.util.Signal;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;


import static it.unicam.quasylab.sibilla.core.tools.stl.CommonForMonitorTesting.getPopulationTrajectory;
import static org.junit.jupiter.api.Assertions.*;

class QuantitativeMonitorTest {


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
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        Signal s = QuantitativeMonitor.atomicFormula((PopulationState sign)-> sign.getOccupancy(0) - 3.0).monitor(t);
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
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        QuantitativeMonitor<PopulationState> atomicMonitor = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0));
        Signal s = QuantitativeMonitor.negation(atomicMonitor).monitor(t);

        assertEquals(s.valueAt(0.5),-0.0);
        assertEquals(s.valueAt(1.5),-8.0);
    }

    @Test
    public void testConjunctionAndDisjunction(){
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

        assertEquals(6.0, conjunctionSignal.valueAt(1.5));
        assertEquals(8.0, disjunctionSignal.valueAt(1.5));

        double timeToCheck = 0.0;
        for (int i = 0; i < 14; i++) {
            assertTrue(conjunctionSignal.valueAt(timeToCheck) <= disjunctionSignal.valueAt(timeToCheck));
            timeToCheck += 0.5;
        }

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
                new int[]{0, 0, 0, 1, 1, 2, 3, 3}
        );

        QuantitativeMonitor<PopulationState> aM = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0) - 2.0);

        Interval interval1 = new Interval(0,3);
        Interval interval2 = new Interval(3,6);

        QuantitativeMonitor<PopulationState> eventuallyBetween0and3 = QuantitativeMonitor.eventually(interval1,aM);
        QuantitativeMonitor<PopulationState> eventuallyBetween4and6 = QuantitativeMonitor.eventually(interval2,aM);

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
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        QuantitativeMonitor<PopulationState> atomicMonitor =QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0)- 1.0);
        Signal s = QuantitativeMonitor.globally(new Interval(1,4),atomicMonitor).monitor(t);

        assertEquals(s.valueAt(0.0),0.0);
        assertTrue(
                QuantitativeMonitor.globally(new Interval(0,3),atomicMonitor).monitor(t).valueAt(0)
                        <
                        QuantitativeMonitor.globally(new Interval(1,4),atomicMonitor).monitor(t).valueAt(0)
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
                new int[]{0, 8, 3, 2, 1, 1, 1}
        );

        QuantitativeMonitor<PopulationState> atomicMonitor = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0)- 6.0);

        Signal s = QuantitativeMonitor.eventually(new Interval(0,4),atomicMonitor).monitor(t);

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
                new int[]{0, 2, 3, 3, 3, 5, 1}
        );

        QuantitativeMonitor<PopulationState> aM1 = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0) - 2.0);
        QuantitativeMonitor<PopulationState> aM2 = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0)- 4.0);

        Signal s = QuantitativeMonitor.until(aM1,new Interval(2,4),aM2).monitor(t);

        assertEquals(s.getEnd(), 6.0);
        assertEquals(s.valueAt(0.0),-2.0);

    }


    @Test
    public void testEventuallyTimeShift(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new int[]{1, 1, 1, 1, 3, 4, 3, 3}
        );

        QuantitativeMonitor<PopulationState> aM = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0) - 2.0);

        QuantitativeMonitor<PopulationState> eventuallyFrom0to3 = QuantitativeMonitor.eventually(new Interval(0,3),aM);
        QuantitativeMonitor<PopulationState> eventuallyFrom3to5 = QuantitativeMonitor.eventually(new Interval(3,5),aM);

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
                new int[]{0, 4, 3, 2, 1, 1, 1}
        );

        QuantitativeMonitor<PopulationState> atomicMonitor = QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(0)- 6.0);

        Signal s = QuantitativeMonitor.eventually(new Interval(0,4),atomicMonitor).monitor(t);
        assertEquals(s.valueAt(0.0),-2.0);


    }

    @Test
    public void testEventuallySIR(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{11, 3, 28, 28, 40, 20, 16, 6, 6, 14, 77, 62, 49},
                new int[]{95,95,94,94,94,93,92,91,91,91,91,91,91,91},
                new int[]{ 5, 4, 5, 4, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0},
                new int[]{ 0, 1, 1, 2, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9}
        );


        QuantitativeMonitor<PopulationState> atomicMonitor =  QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(2) - 25);

        Signal s = QuantitativeMonitor.eventually(new Interval(0,500),atomicMonitor).monitor(t);

        assertEquals(s.valueAt(0.0),-17.0);

    }

    @Test
    public void testNestedOperator(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{11, 3, 28, 28, 40, 20, 16, 6, 6, 14, 77, 62, 49},
                new int[]{95,95,94,94,94,93,92,91,91,91,91,91,91,91},
                new int[]{ 5, 4, 5, 4, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0},
                new int[]{ 0, 1, 1, 2, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9}
        );


        QuantitativeMonitor<PopulationState> atomicMonitor =  QuantitativeMonitor.atomicFormula(s -> s.getOccupancy(2) - 25);

        // F[100,150](x >25 & F[1,20](x>25)  )
        Signal s = QuantitativeMonitor
                .eventually(new Interval(1,120),QuantitativeMonitor
                        .conjunction(atomicMonitor,QuantitativeMonitor.eventually(new Interval(1,20),atomicMonitor))).monitor(t);

        assertEquals(s.valueAt(0.0),-22.0);

    }



    @Test
    public void testComputeProbability(){

        List<Trajectory<PopulationState>> supplierTrajectoryList = new ArrayList<>();
        supplierTrajectoryList.add(
                getPopulationTrajectory(
                        new double[]{1.0, 1.0, 2.0, 1.0, 1.0, 3.0, 1.0},
                        new int[]{2, 2, 3, 3, 3, 5, 1}
                )
        );
        supplierTrajectoryList.add(
                getPopulationTrajectory(
                        new double[]{1.0, 1.0, 2.0, 1.0, 1.0, 3.0, 1.0},
                        new int[]{0, 0, 0, 0, 0, 0, 0}
                )
        );
        supplierTrajectoryList.add(
                getPopulationTrajectory(
                        new double[]{1.0, 1.0, 2.0, 1.0, 1.0, 3.0, 1.0},
                        new int[]{0, 1, 0, 0, 0, 0, 0}
                )
        );
        Random random = new Random();
        Supplier<Trajectory<PopulationState>> trajectorySupplier = () -> supplierTrajectoryList.get(random.nextInt(supplierTrajectoryList.size()));

        QuantitativeMonitor<PopulationState> am = QuantitativeMonitor.atomicFormula(pm -> pm.getOccupancy(0));

        double[] timeSteps = {1.0 , 2.0 , 3.0 , 4.0 , 5.0 , 6.0 , 7.0};
        double[] probabilities = QuantitativeMonitor.computeProbability(am, trajectorySupplier, 100, timeSteps);

        assertEquals(probabilities[0],0.66,0.15);
        assertEquals(probabilities[1],0.33,0.15);

        List<Trajectory<PopulationState>> trajectoryList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            trajectoryList.add(trajectorySupplier.get());
        }
        double[] probabilitiesFromList = QuantitativeMonitor.computeProbability(am,trajectoryList,timeSteps);
        assertEquals(probabilitiesFromList[0],0.66,0.15);
        assertEquals(probabilitiesFromList[1],0.33,0.15);

    }


    @Test
    public void testMeanRobustness(){
        QuantitativeMonitor<PopulationState> am = QuantitativeMonitor.atomicFormula(pm -> pm.getOccupancy(0));
        double[] timeSteps = { 0.5, 1.5, 2.5};
        double[] meanValuesFromSupplier = QuantitativeMonitor.computeMeanRobustness(am,supplierOfTrajectoryForTest(3),100,timeSteps);
        double[] meanValuesFromList = QuantitativeMonitor.computeMeanRobustness(am,listOfTrajectoryForTest(),timeSteps);
        assertEquals(meanValuesFromList[0],1.0);
        assertEquals(meanValuesFromList[1],353.3, 1.0);
        assertEquals(meanValuesFromList[2], 2.5);
        assertEquals(meanValuesFromList[0],meanValuesFromSupplier[0]);
        assertEquals(meanValuesFromList[1],meanValuesFromSupplier[1],25);
        assertEquals(meanValuesFromList[2],meanValuesFromSupplier[2],0.25);
    }

    @Test
    public void testNormalDistributionRobustness(){
        QuantitativeMonitor<PopulationState> am = QuantitativeMonitor.atomicFormula(pm -> pm.getOccupancy(0));
        double[] timeSteps = { 0.5, 1.5, 2.5};
        NormalDistribution[] normDistFromSupplier = QuantitativeMonitor.computeNormalDistributionRobustness(am,supplierOfTrajectoryForTest(2),100,timeSteps);
        NormalDistribution[] normDistFromList = QuantitativeMonitor.computeNormalDistributionRobustness(am,listOfTrajectoryForTest(),timeSteps);

        assertEquals(1,normDistFromList[0].getMean());
        assertEquals(1,normDistFromSupplier[0].getMean());
        assertEquals(0,normDistFromList[0].getStandardDeviation(),1e-3);
        assertEquals(0,normDistFromSupplier[0].getStandardDeviation(),1e-3);

        assertEquals(353.333,normDistFromList[1].getMean(),1e-2);
        assertEquals(357.0,normDistFromSupplier[1].getMean(),1e-3);
        assertEquals(807.605,normDistFromList[1].getStandardDeviation(),1e-3);
        assertEquals(748.048,normDistFromSupplier[1].getStandardDeviation(),1e-3);

        assertEquals(2.5,normDistFromList[2].getMean(),1e-3);
        assertEquals(2.44,normDistFromSupplier[2].getMean(),1e-3);
        assertEquals(0.547,normDistFromList[2].getStandardDeviation(),1e-3);
        assertEquals(0.498,normDistFromSupplier[2].getStandardDeviation(),1e-3);

    }



    @Test
    public void testMeanAndStandardDeviationRobustness() {
        QuantitativeMonitor<PopulationState> am = QuantitativeMonitor.atomicFormula(pm -> pm.getOccupancy(0));
        double[] timeSteps = {0.5, 1.5, 2.5};

        double[][] meanAndSdFromSupplier = QuantitativeMonitor.meanAndStandardDeviationRobustness(
                am,
                supplierOfTrajectoryForTest(2),
                100,
                timeSteps
        );

        double[][] meanAndSdFromList = QuantitativeMonitor.meanAndStandardDeviationRobustness(
                am,
                listOfTrajectoryForTest(),
                timeSteps
        );

        assertEquals(1, meanAndSdFromList[0][1], 0.1);
        assertEquals(1, meanAndSdFromSupplier[0][1], 0.1);
        assertEquals(0, meanAndSdFromList[0][2], 0.1);
        assertEquals(0, meanAndSdFromSupplier[0][2], 0.1);

        assertEquals(353.333, meanAndSdFromList[1][1], 0.1);
        assertEquals(374.1, meanAndSdFromSupplier[1][1], 0.1);
        assertEquals(807.605, meanAndSdFromList[1][2], 0.1);
        assertEquals(766.192, meanAndSdFromSupplier[1][2], 0.1);

        assertEquals(2.5, meanAndSdFromList[2][1], 0.1);
        assertEquals(2.56, meanAndSdFromSupplier[2][1], 0.1);
        assertEquals(0.547, meanAndSdFromList[2][2], 0.1);
        assertEquals(0.498, meanAndSdFromSupplier[2][2], 0.1);
    }


//    @Test
//    public void testMeanAndStandardDeviationRobustnessListDT() {
//        QuantitativeMonitor<PopulationState> am =QuantitativeMonitor.globally(new Interval(0.0,0.5), QuantitativeMonitor.atomicFormula(pm -> pm.getOccupancy(0)));
//        double[][] meanAndSdFromList = QuantitativeMonitor.meanAndStandardDeviationRobustness(
//                am,
//                listOfTrajectoryForTest(),
//                0.5
//        );
//
//        assertEquals(1, meanAndSdFromList[1][1], 1e-3);
//        assertEquals(0, meanAndSdFromList[1][2], 1e-3);
//
//        assertEquals(353.333, meanAndSdFromList[3][1], 1e-2);
//        assertEquals(807.605, meanAndSdFromList[3][2], 1e-3);
//
//        assertEquals(2.5, meanAndSdFromList[5][1], 1e-3);
//        assertEquals(0.547, meanAndSdFromList[5][2], 1e-3);
//    }




    private List<Trajectory<PopulationState>> listOfTrajectoryForTest(){
        List<Trajectory<PopulationState>> trajectoryList = new ArrayList<>();
        trajectoryList.add(getPopulationTrajectory(new int[]{ 1,  100, 2}));
        trajectoryList.add(getPopulationTrajectory(new int[]{ 1,    0, 3}));
        trajectoryList.add(getPopulationTrajectory(new int[]{ 1,   10, 2}));
        trajectoryList.add(getPopulationTrajectory(new int[]{ 1, 2000, 3}));
        trajectoryList.add(getPopulationTrajectory(new int[]{ 1,    0, 2}));
        trajectoryList.add(getPopulationTrajectory(new int[]{ 1,   10, 3}));
        return trajectoryList;
    }


    private Supplier<Trajectory<PopulationState>>  supplierOfTrajectoryForTest(long seed){
        Random random = new Random(seed);
        return () -> listOfTrajectoryForTest().get(random.nextInt(listOfTrajectoryForTest().size()));
    }




}