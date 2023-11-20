package it.unicam.quasylab.sibilla.core.tools.stl;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualitativeMonitorTest {

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


    @Test
    public void testAtomicMonitor(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        BooleanSignal bs =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 2 ).monitor(t);

        System.out.println(bs);

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
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        QualitativeMonitor<PopulationState> bs =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 2 );
        BooleanSignal nbs = QualitativeMonitor.negation(bs).monitor(t);
        System.out.println(nbs);


    }

    @Disabled
    @Test
    public void testConjunctionAndDisjunction(){
        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                2,
                new double[]{0.0, 8.0, 3.0, 2.0, 2.0, 1.0, 1.0},
                new double[]{2.0, 6.0, 1.0, 1.0, 1.0, 0.0, 0.0}
        );


        QualitativeMonitor<PopulationState> leftAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 2 );
        QualitativeMonitor<PopulationState> rightAtomic =
                QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(1) <= 0 );


        //System.out.println(leftAtomic.monitor(t));
        //System.out.println(rightAtomic.monitor(t));


        QualitativeMonitor<PopulationState> conjunction = QualitativeMonitor.conjunction(leftAtomic,rightAtomic);
        QualitativeMonitor<PopulationState> disjunction= QualitativeMonitor.disjunction(leftAtomic,rightAtomic);

        BooleanSignal cbs = conjunction.monitor(t);
        BooleanSignal dbs = disjunction.monitor(t);
//
//        System.out.println(cbs);
//        System.out.println(dbs);

    }




}