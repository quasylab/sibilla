package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class FitnessFunction {

    private final Function<Map<String,Double>,Double> fitnessFunction;
    private List<Predicate<Map<String,Double>>> constraints;
    private double penaltyValue;

    public FitnessFunction(Function<Map<String,Double>,Double> fitnessFunction){
        this(fitnessFunction,null,0.0);
    }

    public FitnessFunction(Function<Map<String,Double>,Double> fitnessFunction,List<Predicate<Map<String,Double>>> constraints){
        this(fitnessFunction,constraints,0.0);
    }

    public FitnessFunction(Function<Map<String,Double>,Double> fitnessFunction, List<Predicate<Map<String,Double>>> constraints, double penaltyValue){
        this.fitnessFunction = fitnessFunction;
        this.constraints = constraints;
        this.penaltyValue = penaltyValue;
    }

    public double evaluate(Map<String,Double> parameters){
        if(constraints != null){
            boolean anyConstrainViolated = constraints.stream().anyMatch( p -> !p.test(parameters) );
            if(anyConstrainViolated)
                return penaltyValue;
        }
        return fitnessFunction.apply(parameters);
    }

    public void setPenaltyValue(double penaltyValue) {
        this.penaltyValue = penaltyValue;
    }

    public void addConstraints( List<Predicate<Map<String,Double>>> constraints){
        if(this.constraints == null)
            this.constraints = new ArrayList<>();
        this.constraints.addAll(constraints);
    }
}
