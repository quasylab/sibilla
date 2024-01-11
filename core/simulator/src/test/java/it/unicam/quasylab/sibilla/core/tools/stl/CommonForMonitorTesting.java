package it.unicam.quasylab.sibilla.core.tools.stl;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;

import java.util.Arrays;

public class CommonForMonitorTesting {

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



}
