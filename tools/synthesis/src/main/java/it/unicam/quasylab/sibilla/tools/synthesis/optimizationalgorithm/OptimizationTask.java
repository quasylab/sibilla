package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public interface OptimizationTask {
    Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String,Double>>> constraints, Properties properties,Long seed);

    default Map<String, Double> maximize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints, Properties properties, Long seed) {
        ToDoubleFunction<Map<String, Double>> negativeObjectiveFunction = map -> -1 * objectiveFunction.applyAsDouble(map);
        return minimize(negativeObjectiveFunction,searchSpace,constraints,properties,seed);
    }

    default  Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String,Double>>> constraints, Properties properties){
        return minimize(objectiveFunction,searchSpace,constraints,properties,System.nanoTime());
    }

    default Map<String, Double> maximize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints, Properties properties) {
        ToDoubleFunction<Map<String, Double>> negativeObjectiveFunction = map -> -1 * objectiveFunction.applyAsDouble(map);
        return minimize(negativeObjectiveFunction,searchSpace,constraints,properties,System.nanoTime());
    }
    default Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints){
        return minimize(objectiveFunction,searchSpace,constraints,new Properties(),System.nanoTime());
    }

    default Map<String,Double> maximize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints){
        return maximize(objectiveFunction,searchSpace, constraints,new Properties(),System.nanoTime());
    }

    default Map<String,Double> minimize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        return minimize(objectiveFunction,searchSpace,new ArrayList<>(),new Properties(),System.nanoTime());
    }

    default Map<String,Double> maximize(ToDoubleFunction<Map<String,Double>> objectiveFunction, HyperRectangle searchSpace){
        return maximize(objectiveFunction,searchSpace, new ArrayList<>(),new Properties(),System.nanoTime());
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
