package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import java.util.*;

public interface PollMethod {

    List<Map<String, Double>> getPolledPoints(Map<String, Double> point, double deltaMesh);


}
