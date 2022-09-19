package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.ParticleSwarmOptimization;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_NO_SUCH_SURROGATE;

public class SurrogateFactory {

    public static Surrogate getSurrogate(String surrogateName, Properties properties){
        if(surrogateName.equals("rfr"))
            return new RandomForestSurrogate(properties);
        else
            throw new IllegalArgumentException(EXCEPT_NO_SUCH_SURROGATE + " : "+ surrogateName);
    }

    public static String[]  getSurrogatesList(){
        return  new String[]{"rfr"};
    }
}
