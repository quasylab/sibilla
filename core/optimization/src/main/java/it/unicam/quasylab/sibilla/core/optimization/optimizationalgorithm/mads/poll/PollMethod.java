package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.AbstractMADSTask;

import java.util.*;

public interface PollMethod {
    //List<Map<String, Double>> getPolledPoints(Map<String, Double> point, double deltaMesh,Random random);
    List<Map<String, Double>> getPolledPoints(AbstractMADSTask madsTaskInstance);

}
