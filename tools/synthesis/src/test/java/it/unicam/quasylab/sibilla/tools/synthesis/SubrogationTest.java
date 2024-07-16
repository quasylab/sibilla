package it.unicam.quasylab.sibilla.tools.synthesis;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.pso.PSOAlgorithm;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.RandomForest;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.tools.synthesis.CommonForTesting.ROSENBROCK_FUNCTION;
import static it.unicam.quasylab.sibilla.tools.synthesis.CommonForTesting.SIMPLE_FUNCTION;

class SubrogationTest {



    @Disabled
    @Test
    void testSubrogationRosenberg(){

        // DEFINITION OF THE SEARCH SPACE

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x0",-5.0,5.0),
                new ContinuousInterval("x1",-5.0,5.0),
                new ContinuousInterval("x2",-5.0,5.0)
        );


        // OPTIMIZATION WITHOUT SURROGATE

        OptimizationTask optimizationTask = new PSOAlgorithm().getOptimizationTask();
        Map<String,Double> minimizingValuesFunction  = optimizationTask.minimize(ROSENBROCK_FUNCTION,searchSpace);

        // OPTIMIZATION WITH SURROGATE

        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(ROSENBROCK_FUNCTION,
                new LatinHyperCubeSamplingTask(),searchSpace,
                10000,
                0.85,
                new Properties());

        ToDoubleFunction<Map<String,Double>> surrogateFunction = randomForestModel.getSurrogateFunction(true);
        Map<String,Double> minimizingValuesSurrogate  = optimizationTask.minimize(surrogateFunction,searchSpace);

        // RESULT COMPARISON

        System.out.println("result from real function:");
        System.out.println(minimizingValuesFunction);
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");
        System.out.println("result from surrogate function:");
        System.out.println(minimizingValuesSurrogate);
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");

        System.out.println("distance coordinates : "+distanceCoordinates(minimizingValuesFunction,minimizingValuesSurrogate));
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");


    }

    @Disabled
    @Test
    void testSubrogationSimpleFunction(){
        // DEFINITION OF THE SEARCH SPACE

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",-2.0,2.0),
                new ContinuousInterval("y",-2.0,2.0)
        );


        // OPTIMIZATION WITHOUT SURROGATE

        OptimizationTask optimizationTask = new PSOAlgorithm().getOptimizationTask();
        Map<String,Double> minimizingValuesFunction  = optimizationTask.minimize(SIMPLE_FUNCTION,searchSpace);

        // OPTIMIZATION WITH SURROGATE

        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(SIMPLE_FUNCTION, new LatinHyperCubeSamplingTask(),searchSpace,1000,0.85,new Properties());

        ToDoubleFunction<Map<String,Double>> surrogateFunction = randomForestModel.getSurrogateFunction(true);
        Map<String,Double> minimizingValuesSurrogate  = optimizationTask.minimize(surrogateFunction,searchSpace);

        // RESULT COMPARISON

        System.out.println("values from real function:");
        System.out.println(minimizingValuesFunction);
        System.out.println("result "+SIMPLE_FUNCTION.applyAsDouble(minimizingValuesFunction));
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");
        System.out.println("values from surrogate function:");
        System.out.println(minimizingValuesSurrogate);
        System.out.println("result "+SIMPLE_FUNCTION.applyAsDouble(minimizingValuesSurrogate));

        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");

        System.out.println("distance coordinates : "+distanceCoordinates(minimizingValuesFunction,minimizingValuesSurrogate));
        System.out.println("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");


    }


    private double distanceCoordinates( Map<String,Double> values1,  Map<String,Double> values2){
        double distance = 0;
        for (String key:values1.keySet()) {
            distance += Math.pow(values1.get(key)-values2.get(key),2.0);
        }
        return Math.sqrt(distance);
    }




}
