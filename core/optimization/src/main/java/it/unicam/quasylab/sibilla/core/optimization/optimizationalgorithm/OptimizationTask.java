package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public interface OptimizationTask {
    Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String,Double>>> constraints);

    default Map<String, Double> maximize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints) {
        ToDoubleFunction<Map<String, Double>> negativeObjectiveFunction = map -> -1 * objectiveFunction.applyAsDouble(map);
        return minimize(negativeObjectiveFunction,searchSpace,constraints);
    }

    default Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        List<Predicate<Map<String,Double>>> emptyConstraintsList = new ArrayList<>();
        return minimize(objectiveFunction,searchSpace,emptyConstraintsList);
    };
    default Map<String,Double> maximize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        List<Predicate<Map<String,Double>>> emptyConstraintsList = new ArrayList<>();
        return maximize(objectiveFunction,searchSpace,emptyConstraintsList);
    };


    default Map<String,Double> minimizeWithinTheSearchSpace(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String,Double>>> constraints){
        constraints.addAll(getSearchSpaceAsConstraintList(searchSpace));
        return minimize(objectiveFunction,searchSpace,constraints);
    }

    default Map<String,Double> maximizeWithinTheSearchSpace(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String,Double>>> constraints){
        constraints.addAll(getSearchSpaceAsConstraintList(searchSpace));
        return maximize(objectiveFunction,searchSpace,constraints);
    }

    default Map<String,Double> minimizeWithinTheSearchSpace(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        List<Predicate<Map<String, Double>>> constraints = new ArrayList<>(getSearchSpaceAsConstraintList(searchSpace));
        return minimize(objectiveFunction,searchSpace,constraints);
    }

    default Map<String,Double> maximizeWithinTheSearchSpace(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        List<Predicate<Map<String, Double>>> constraints = new ArrayList<>(getSearchSpaceAsConstraintList(searchSpace));
        return maximize(objectiveFunction,searchSpace,constraints);
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
