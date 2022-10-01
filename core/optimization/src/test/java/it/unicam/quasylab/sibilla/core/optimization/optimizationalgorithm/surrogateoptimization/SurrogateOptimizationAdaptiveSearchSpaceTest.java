package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.surrogateoptimization;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.ROSENBROCK_FUNCTION;

class SurrogateOptimizationAdaptiveSearchSpaceTest {

//    @Test
//    void testRosenbrockFunction(){
//
//        // FUNCTION TO BE OPTIMIZED
//        Function<Map<String,Double>,Double> functionToOptimize = ROSENBROCK_FUNCTION;
//
//        // CONSTRAINTS
//        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
//
//        // SEARCH SPACE
//        HyperRectangle searchSpace = new HyperRectangle(
//                new Interval("x1",-10.0,10.0),
//                new Interval("x2",-10.0,10.0),
//                new Interval("x3",-10.0,10.0)
//        );
//
//        SurrogateOptimizationAdaptiveSearchSpace surrogateOptimizationAdaptiveSearchSpace = new SurrogateOptimizationAdaptiveSearchSpace(
//                "rfr",
//                "pso",
//                functionToOptimize,
//                constraints,
//                "lhs",
//                searchSpace,
//                1000,
//                null,
//                null,
//                new Properties()
//                );
//
//        Map<String,Double> solution = surrogateOptimizationAdaptiveSearchSpace.minimize();
//        System.out.println(solution);
//    }

}