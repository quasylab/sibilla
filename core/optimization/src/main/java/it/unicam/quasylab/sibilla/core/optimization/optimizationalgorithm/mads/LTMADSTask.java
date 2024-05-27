package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll.LTPollMaximalPositiveBasis;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll.LTPollMinimalPositiveBasis;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class LTMADSTask extends AbstractMADSTask{



    @Override
    public Map<String, Double> minimize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints, Properties properties, Long seed) {
        this.setUsePositiveBasis(properties);
        return super.minimize(objectiveFunction, searchSpace, constraints, properties,seed);
    }

    private void setUsePositiveBasis(Properties properties){
        boolean DEFAULT_MINIMAL_BASIS = true;
        boolean useMinimalBasis = Boolean.getBoolean(properties.getProperty("mads.minimal_basis",  Boolean.toString(DEFAULT_MINIMAL_BASIS)));
        super.pollMethod = useMinimalBasis ? new LTPollMinimalPositiveBasis() : new LTPollMaximalPositiveBasis();
    }


}
