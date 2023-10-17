package it.unicam.quasylab.sibilla.core.tools.stl;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import it.unicam.quasylab.sibilla.core.util.Interval;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

class QualitativeMonitorTest {
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
        QualitativeMonitor.AtomicMonitor<PopulationState> am = new QualitativeMonitor.AtomicMonitor<>(
                s -> s.getOccupancy(0),
                v -> v >= 2
        );
        BooleanSignal bs = am.monitor(getTestTrajectory_1());
        LinkedList<Interval> intervals = bs.getIntervals();
    }




    @Test
    public void testEventuallyMonitor(){
        QualitativeMonitor.AtomicMonitor<PopulationState> am_1 = new QualitativeMonitor.AtomicMonitor<>(
                s -> s.getOccupancy(0),
                v -> v >= 2
        );
        QualitativeMonitor.AtomicMonitor<PopulationState> am_2 = new QualitativeMonitor.AtomicMonitor<>(
                s -> s.getOccupancy(1),
                v -> v >= 2
        );
        QualitativeMonitor.FinallyMonitor<PopulationState> fm_1 = new QualitativeMonitor
                .FinallyMonitor<>(am_1,new Interval(0,1));
        QualitativeMonitor.FinallyMonitor<PopulationState> fm_2 = new QualitativeMonitor
                .FinallyMonitor<>(am_2,new Interval(0,1));

        QualitativeMonitor.ConjunctionMonitor<PopulationState> cm = new QualitativeMonitor.ConjunctionMonitor<>(
                fm_1,
                fm_2
        );

        BooleanSignal bs = cm.monitor(getTestTrajectory_1());
        LinkedList<Interval> intervals = bs.getIntervals();
    }

}