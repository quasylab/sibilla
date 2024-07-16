package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll.OrthogonalPollMaximalPositiveBasis;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class OrthoMADSTask extends AbstractMADSTask {
    @Override
    public Map<String, Double> minimize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints, Properties properties, Long seed) {
       super.pollMethod = new OrthogonalPollMaximalPositiveBasis();
       return super.minimize(objectiveFunction, searchSpace, constraints, properties,seed);
    }
}
