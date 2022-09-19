package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.ParticleSwarmOptimization;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_NO_SUCH_OPTIMIZATION_ALGORITHM;

public class OptimizationStrategyFactory {

    public static OptimizationStrategy getOptimizationStrategy(String optimizationStrategyName,
                                                        Function<Map<String,Double>,Double> functionToOptimize,
                                                        HyperRectangle searchSpace,
                                                        Properties properties){
       if(optimizationStrategyName.equals("pso"))
           return new ParticleSwarmOptimization(functionToOptimize,searchSpace,properties);
       else
           throw new IllegalArgumentException(EXCEPT_NO_SUCH_OPTIMIZATION_ALGORITHM + " : "+ optimizationStrategyName);
    }

    public static OptimizationStrategy getOConstrainedOptimizationStrategy(String optimizationStrategyName,
                                                                    Function<Map<String,Double>,Double> functionToOptimize,
                                                                    List<Predicate<Map<String,Double>>> constraints,
                                                                    HyperRectangle searchSpace,
                                                                    Properties properties){
        if(optimizationStrategyName.equals("pso"))
            return new ParticleSwarmOptimization(functionToOptimize,constraints,searchSpace,properties);
        else
            throw new IllegalArgumentException(EXCEPT_NO_SUCH_OPTIMIZATION_ALGORITHM + " : "+ optimizationStrategyName);
    }

    public static String[] getOptimizationStrategiesNameList(){
        return new String[]{"pso"};
    }
}
