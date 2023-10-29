package it.unicam.quasylab.sibilla.core.tools.stl;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
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
    private Trajectory<PopulationState> getTestTrajectory_1(){
        Trajectory<PopulationState> trajectory = new Trajectory<>();

        trajectory.add(0.0, new PopulationState(2,new Population(0,0),new Population(1,2)));
        trajectory.add(1.0, new PopulationState(2,new Population(0,8),new Population(1,6)));
        trajectory.add(2.0, new PopulationState(2,new Population(0,3),new Population(1,1)));
        trajectory.add(3.0, new PopulationState(2,new Population(0,2),new Population(1,1)));
        trajectory.add(4.0, new PopulationState(2,new Population(0,1),new Population(1,1)));
        trajectory.add(5.0, new PopulationState(2,new Population(0,3),new Population(1,1)));
        trajectory.add(6.0, new PopulationState(2,new Population(0,1),new Population(1,0)));

        return trajectory;
    }


    @Test
    public void testAtomicMonitor(){

        Trajectory<PopulationState> t = getPopulationTrajectory(
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                1,
                new double[]{0.0, 8.0, 3.0, 2.0, 1.0, 1.0, 1.0}
        );

        BooleanSignal bs = QualitativeMonitor.atomicFormula( (PopulationState s) -> s.getOccupancy(0) >= 2 ).monitor(t);

        assertFalse(bs.getValueAt(0.5));
        assertTrue(bs.getValueAt(1.0));
        assertTrue(bs.getValueAt(3.5));
        assertFalse(bs.getValueAt(4.0));
        assertFalse(bs.getValueAt(4.5));
    }


}