package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.AbstractMADSTask;

import java.util.List;
import java.util.Map;

public interface PollMethod {
    //List<Map<String, Double>> getPolledPoints(Map<String, Double> point, double deltaMesh,Random random);
    List<Map<String, Double>> getPolledPoints(AbstractMADSTask madsTaskInstance);

}
