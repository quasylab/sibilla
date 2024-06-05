package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;
@Disabled
class LTMADSTaskTest {

    private boolean beingInRange(double value, double beginRange ,double endRange){
        return value >= beginRange && value <= endRange;
    }


    /**
     * simple function : <b> 7 * ( x * y )/(e^(x^2+y^2)) </b> minimization
     * An Interval have :
     * <ul>
     * <li>Global minima : <b>None</b>
     * <li>Local minima: <b>Local minima</b> : ( - 0.707107 , + 0.707107 ) , ( + 0.707107 , - 0.707107 )
     * </ul>
     *
     * @see    <a href=https://www.wolframalpha.com/input?i=minimize+7+*+%28+x+*+y+%29%2F%28e%5E%28x%5E2%2By%5E2%29%29">WolframAlhpa</a>
     */
    @Test
    void minimizeFunction() {

        ToDoubleFunction<Map<String,Double>> functionToOptimize = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",-2.0,2.0),
                new ContinuousInterval("y",-2.0,2.0)
        );

        Map<String,Double> minimizingValues = new LTMADSTask().minimize(functionToOptimize,searchSpace);

        boolean isLocalMinima1 = beingInRange(minimizingValues.get("x"),-0.95, -0.55) &&
                beingInRange(minimizingValues.get("y"),0.55, 0.95);

        boolean isLocalMinima2 = beingInRange(minimizingValues.get("x"),0.55, 0.95) &&
                beingInRange(minimizingValues.get("y"),-0.95, -0.55);

        System.out.println(minimizingValues);
        assertTrue(isLocalMinima1 || isLocalMinima2);
    }



    /**
     * simple function : <b> 7 * ( x * y )/(e^(x^2+y^2)) </b> minimization
     * An Interval have :
     * <ul>
     * <li>Global minima : <b>None</b>
     * <li>Local minima: <b>Local minima</b> : ( - 0.707107 , + 0.707107 ) , ( + 0.707107 , - 0.707107 )
     * </ul>
     *
     * @see    <a href=https://www.wolframalpha.com/input?i=minimize+7+*+%28+x+*+y+%29%2F%28e%5E%28x%5E2%2By%5E2%29%29">WolframAlhpa</a>
     */
    @Test
    void minimizeFunctionSeed() {

        ToDoubleFunction<Map<String,Double>> functionToOptimize = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",-2.0,2.0),
                new ContinuousInterval("y",-2.0,2.0)
        );

        Map<String,Double> minimizingValues = new LTMADSTask().minimize(functionToOptimize,searchSpace,new ArrayList<>(),new Properties(),123L);


        assertEquals(0.7264628148152565, minimizingValues.get("x"));
        assertEquals(-0.7234187530700915, minimizingValues.get("y"));

    }

    /**
     * simple function : <b> - 7 * ( x * y )/(e^(x^2+y^2)) </b> minimization
     * An Interval have :
     * <ul>
     * <li>Global maxima : <b>None</b>
     * <li>Local maxima: <b>Local minima</b> : ( - 0.707107 , + 0.707107 ) , ( + 0.707107 , - 0.707107 )
     * </ul>
     *
     * @see    <a href=https://www.wolframalpha.com/input?i=minimize+7+*+%28+x+*+y+%29%2F%28e%5E%28x%5E2%2By%5E2%29%29">WolframAlhpa</a>
     */
    @Test
    void maximaizeFunctionSeed() {

        ToDoubleFunction<Map<String,Double>> functionToOptimize = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",-2.0,2.0),
                new ContinuousInterval("y",-2.0,2.0)
        );

        Map<String,Double> maximizingValues = new LTMADSTask().maximize(functionToOptimize,searchSpace,new ArrayList<>(),new Properties(),123L);
        assertEquals(0.7036325818092801, maximizingValues.get("x"));
        assertEquals(0.7114320316940459, maximizingValues.get("y"));

    }

}