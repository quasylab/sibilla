package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class BarrierFunction {
    private final ToDoubleFunction<Map<String,Double>> function;
    private final List<Predicate<Map<String,Double>>> constraints;

    public BarrierFunction(ToDoubleFunction<Map<String,Double>> function, List<Predicate<Map<String,Double>>> constraints){
        this.function = function;
        this.constraints = Optional.ofNullable(constraints).orElse(new ArrayList<>());
    }

    public double evaluate(Map<String,Double> parameters){
        if(constraints.size()>0){
            double penaltyValue = Double.POSITIVE_INFINITY;
            if(constraints.stream().anyMatch(p -> !p.test(parameters)))
                return penaltyValue;
        }
        return function.applyAsDouble(parameters);
    }
}
