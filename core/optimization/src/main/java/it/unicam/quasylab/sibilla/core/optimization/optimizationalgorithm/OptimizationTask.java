package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public interface OptimizationTask {
    Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String,Double>>> constraints, Properties properties);

    default Map<String, Double> maximize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints, Properties properties) {
        ToDoubleFunction<Map<String, Double>> negativeObjectiveFunction = map -> -1 * objectiveFunction.applyAsDouble(map);
        return minimize(negativeObjectiveFunction,searchSpace,constraints,properties);
    }

    default Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints){
        return minimize(objectiveFunction,searchSpace,constraints,new Properties());
    }
    default Map<String,Double> maximize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints){
        return maximize(objectiveFunction,searchSpace, constraints,new Properties());
    }

    default Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        return minimize(objectiveFunction,searchSpace,new ArrayList<>(),new Properties());
    }
    default Map<String,Double> maximize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        return maximize(objectiveFunction,searchSpace, new ArrayList<>(),new Properties());
    }

    void setProperties(Properties properties);

    default List<Predicate<Map<String,Double>>> getSearchSpaceAsConstraintList(HyperRectangle searchSpace){
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
        for (Interval i :searchSpace.getIntervals()) {
            constraints.add( map -> i.getLowerBound() <= map.get(i.getId()) && map.get(i.getId()) <= i.getUpperBound());
        }
        return constraints;
    }
}
