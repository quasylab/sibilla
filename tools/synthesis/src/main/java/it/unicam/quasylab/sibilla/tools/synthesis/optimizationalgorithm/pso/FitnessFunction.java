package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.pso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 *
 * A fitness function is a particular type of objective function that
 * is used to summarise, as a single figure of merit, how close a given
 * design solution is to achieving the set aims. Fitness functions are
 * used in genetic programming and genetic algorithms to guide simulations
 * towards optimal design solutions.
 *
 * @author      Lorenzo Matteucci
 */
public class FitnessFunction {
    private final ToDoubleFunction<Map<String,Double>> fitnessFunction;
    private final List<Predicate<Map<String,Double>>> constraints;
    private final double penaltyValue;
    public FitnessFunction(ToDoubleFunction<Map<String,Double>> fitnessFunction, List<Predicate<Map<String,Double>>> constraints, double penaltyValue){
        this.fitnessFunction = fitnessFunction;
        this.constraints = Optional.ofNullable(constraints).orElse(new ArrayList<>());
        this.penaltyValue = penaltyValue;
    }

    public double evaluate(Map<String,Double> parameters){
        if(constraints.size()>0){
            if(constraints.stream().anyMatch( p -> !p.test(parameters)))
                return penaltyValue;
        }
        return fitnessFunction.applyAsDouble(parameters);
    }

}
