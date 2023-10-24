package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.core.optimization.CommonForTesting.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for Particle Swarm Optimization
 *
 * @author      Lorenzo Matteucci
 */
class PSOTest {

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

        Map<String,Double> minimizingValues = new PSOTask().minimize(functionToOptimize,searchSpace);

        boolean isLocalMinima1 = beingInRange(minimizingValues.get("x"),-0.95, -0.55) &&
                beingInRange(minimizingValues.get("y"),0.55, 0.95);

        boolean isLocalMinima2 = beingInRange(minimizingValues.get("x"),0.55, 0.95) &&
                beingInRange(minimizingValues.get("y"),-0.95, -0.55);

        assertTrue(isLocalMinima1 || isLocalMinima2);
    }

    @Test
    void maximizeFunction() {

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

        Map<String,Double> maximizingValues = new PSOTask().maximize(functionToOptimize,searchSpace);


        boolean isLocalMaxima1 = beingInRange(maximizingValues.get("x"),-0.95, -0.55) &&
                beingInRange(maximizingValues.get("y"),-0.95, -0.55);

        boolean isLocalMaxima2 = beingInRange(maximizingValues.get("x"),0.55, 0.95) &&
                beingInRange(maximizingValues.get("y"),0.55, 0.95);

        assertTrue(isLocalMaxima1 || isLocalMaxima2);
    }

    /**
     * @see    <a href=https://www.researchgate.net/publication/336121050_Structural_Design_Optimization_Based_on_the_Moving_Baseline_Strategy">Structural Design Optimization Based on the Moving Baseline Strategy</a>
     */
    @Test
    //@Disabled("Disabled : very time consuming")
    void minimizeFunctionWithConstraint() {

        ToDoubleFunction<Map<String,Double>> functionToOptimize = (
                stringDoubleMap -> {
                    double x1 = stringDoubleMap.get("x1");
                    double x2 = stringDoubleMap.get("x2");
                    return ( Math.pow(Math.sin(2 * Math.PI * x1),3) * Math.sin(2 * Math.PI * x2) )/( Math.pow(x1,3) *(x1 + x2) );
                }
        );

        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();

        constraints.add(
                map -> {
                    double x1 = map.get("x1");
                    double x2 = map.get("x2");
                    return Math.pow(x1,2) - x2 +1 <= 0;
                }
        );

        constraints.add(
                map -> {
                    double x1 = map.get("x1");
                    double x2 = map.get("x2");
                    return 1 - x1 + Math.pow((x2 - 4),2) <= 0;
                }
        );

        constraints.add(
                map -> {
                    double x1 = map.get("x1");
                    return 0 <= x1 && x1 <= 10;
                }
        );

        constraints.add(
                map -> {
                    double x2 = map.get("x2");
                    return 0 <= x2 && x2 <= 10;
                }
        );

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x1",-10.0,10.0),
                new ContinuousInterval("x2",-10.0,10.0)
        );


        //Map<String,Double> minimizingValues = new ParticleSwarmOptimization(functionToOptimize,constraints,searchSpace,new Properties()).minimize();

        Map<String,Double> minimizingValues = new PSOTask().minimize(functionToOptimize,searchSpace,constraints);


        HyperRectangle rightZone = new HyperRectangle(
                new ContinuousInterval("x1",0.9,1.4),
                new ContinuousInterval("x2",3.0,4.0)
        );

        assertTrue(rightZone.couldContain(minimizingValues));
    }

    //@Disabled("Disabled : very time consuming")
    @Test
    void minimizeMultidimensionalFunctionWithConstraint() {

        ToDoubleFunction<Map<String,Double>> functionToOptimize = (
                stringDoubleMap -> {
                    double x1 = stringDoubleMap.get("x1");
                    double x2 = stringDoubleMap.get("x2");
                    double x3 = stringDoubleMap.get("x3");
                    double x4 = stringDoubleMap.get("x4");
                    double x5 = stringDoubleMap.get("x5");
                    double x6 = stringDoubleMap.get("x6");
                    double x7 = stringDoubleMap.get("x7");
                    return Math.pow(x1-10,2)
                            + 5*Math.pow(x2-12,2)
                            + Math.pow(x3,4)
                            + 3 * Math.pow(x4-11,2)
                            + 10 * Math.pow(x5,6)
                            + 7 * Math.pow(x6,2)
                            + Math.pow(x7,4)
                            - 4 * x6 * x7
                            - 10 * x6
                            - 8 * x7;
                }
        );

        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();

        constraints.add(
                map -> {
                    double x1 = map.get("x1");
                    double x2 = map.get("x2");
                    double x3 = map.get("x3");
                    double x4 = map.get("x4");
                    double x5 = map.get("x5");
                    return 127 - 2 * Math.pow(x1,2) - 3 * Math.pow(x2,4) - x3 -4 * Math.pow(x4,2) - 5 * x5 >= 0;
                }
        );

        constraints.add(
                map -> {
                    double x1 = map.get("x1");
                    double x2 = map.get("x2");
                    double x3 = map.get("x3");
                    double x4 = map.get("x4");
                    double x5 = map.get("x5");
                    return 282 - 7 * x1 - 3 * x2 - 10 * Math.pow(x3,2) - x4 + x5 >= 0;
                }
        );

        constraints.add(
                map -> {
                    double x1 = map.get("x1");
                    double x2 = map.get("x2");
                    double x6 = map.get("x6");
                    double x7 = map.get("x7");
                    return 196 - 23*x1 - Math.pow(x2,2) - 6 * Math.pow(x6,2) + 8 * x7>= 0;
                }
        );

        constraints.add(
                map -> {
                    double x1 = map.get("x1");
                    double x2 = map.get("x2");
                    double x3 = map.get("x3");
                    double x6 = map.get("x6");
                    double x7 = map.get("x7");
                    return -4 * Math.pow(x1,2) - Math.pow(x2,2) + 3 * x1 * x2 - 2 * Math.pow(x3,2) - 5 * x6 + 11 * x7 >= 0;
                }
        );

        constraints.add(
                map -> {
                    boolean x1 = -10 <= map.get("x1") && map.get("x1") <= 10;
                    boolean x2 = -10 <= map.get("x2") && map.get("x2") <= 10;
                    boolean x3 = -10 <= map.get("x3") && map.get("x3") <= 10;
                    boolean x4 = -10 <= map.get("x4") && map.get("x4") <= 10;
                    boolean x5 = -10 <= map.get("x5") && map.get("x5") <= 10;
                    boolean x6 = -10 <= map.get("x6") && map.get("x6") <= 10;
                    boolean x7 = -10 <= map.get("x7") && map.get("x7") <= 10;
                    return x1 && x2 && x3 && x4 && x5 && x6 && x7 ;
                }
        );

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x1",-10.0,10.0),
                new ContinuousInterval("x2",-10.0,10.0),
                new ContinuousInterval("x3",-10.0,10.0),
                new ContinuousInterval("x4",-10.0,10.0),
                new ContinuousInterval("x5",-10.0,10.0),
                new ContinuousInterval("x6",-10.0,10.0),
                new ContinuousInterval("x7",-10.0,10.0)
        );

        //Map<String,Double> minimizingValues = new ParticleSwarmOptimization(functionToOptimize,constraints,searchSpace,new Properties()).minimize();

        Map<String,Double> minimizingValues = new PSOTask().minimize(functionToOptimize,searchSpace,constraints);

        double min = functionToOptimize.applyAsDouble(minimizingValues);
        assertTrue(min>650 && min<700);

    }


    @Test
    public void testRosenbrockFunction(){

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x1",-10.0,10.0),
                new ContinuousInterval("x2",-10.0,10.0),
                new ContinuousInterval("x3",-10.0,10.0)
        );

        Properties psoProperties = new Properties();
        psoProperties.setProperty("pso.iteration", "1000");
        psoProperties.setProperty("pso.particles_number", "1000");
        //ParticleSwarmOptimization pso =  new ParticleSwarmOptimization(ROSENBROCK_FUNCTION,null,searchSpace,psoProperties);

        //pso.setSearchSpaceAsConstraints();

        //Map<String,Double> minimizingValues = pso.minimize();
        //System.out.println(minimizingValues.toString());

        OptimizationTask optimizationTask = new PSOAlgorithm().getOptimizationTask();
        optimizationTask.setProperties(psoProperties);
        Map<String,Double> minimizingValues  = optimizationTask.minimize(ROSENBROCK_FUNCTION,searchSpace);


        HyperRectangle rightZone = new HyperRectangle(
                new ContinuousInterval("x1",0.5,1.5),
                new ContinuousInterval("x2",0.5,1.5),
                new ContinuousInterval("x3",0.5,1.5)
        );

        assertTrue(rightZone.couldContain(minimizingValues));
    }




    @Test
    void minimizeSimpleFunctionWithConstraints(){

        // SEARCH SPACE
        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",200.0,400.0),
                new ContinuousInterval("y",0.03, 3.0),
                new ContinuousInterval("w",1.0,10.0)
        );

        // FUNCTION
        ToDoubleFunction<Map<String,Double>> myFunction = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    double w = stringDoubleMap.get("w");
                    double z;
                    boolean conditionOnX = x >= 200 && x <= 400;
                    boolean conditionOnY = y >= 0.03 && y <= 3.0;
                    boolean conditionOnW = w >= 1.0 && w <= 10.0;
                    if( conditionOnX && conditionOnY && conditionOnW )
                        z = 111.0;
                    else
                        z = 55.0;
                    return z;
                }
        );

        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
        constraints.add( map -> {
            boolean xConstraint = map.get("x") >= 200 && map.get("x") <= 400;
            boolean yConstraint = map.get("y") >= 0.03 && map.get("y") <= 3.0;
            boolean wConstraint = map.get("w") >= 1.0 && map.get("w") <= 10.0;
            return xConstraint && yConstraint && wConstraint;
        });

        Properties properties = new Properties();

        properties.put("pso.particles_number","5");
        properties.put("pso.iteration","3");


        OptimizationTask optimizationTask = new PSOAlgorithm().getOptimizationTask();
        optimizationTask.setProperties(properties);
        Map<String,Double> solution = optimizationTask.minimize(myFunction,searchSpace,constraints);

        double result = myFunction.applyAsDouble(solution);
        assertEquals(111.0, result);
    }

    @Test
    void minimizeSimpleFunctionSearchSpaceAsConstraints(){

        // SEARCH SPACE
        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",200.0,400.0),
                new ContinuousInterval("y",0.03, 3.0),
                new ContinuousInterval("w",1.0,10.0)
        );

        // FUNCTION
        ToDoubleFunction<Map<String,Double>> myFunction = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    double w = stringDoubleMap.get("w");
                    double z;
                    boolean conditionOnX = x >= 200 && x <= 400;
                    boolean conditionOnY = y >= 0.03 && y <= 3.0;
                    boolean conditionOnW = w >= 1.0 && w <= 10.0;
                    if( conditionOnX && conditionOnY && conditionOnW )
                        z = 111.0;
                    else
                        z = 55.0;
                    return z;
                }
        );


        Properties properties = new Properties();

        properties.put("pso.particles_number","100");
        properties.put("pso.iteration","100");


        OptimizationTask optimizationTask = new PSOAlgorithm().getOptimizationTask();
        optimizationTask.setProperties(properties);
        Map<String,Double> solution = optimizationTask.minimize(myFunction,searchSpace);
//        ParticleSwarmOptimization pso = new ParticleSwarmOptimization(myFunction,null,searchSpace,properties);
//        pso.setSearchSpaceAsConstraints();
//        Map<String,Double> solution = pso.minimize();
        double result = myFunction.applyAsDouble(solution);

        assertEquals(111.0, result);
    }


    @Test
    void testMaximizationSimpleFunction(){
        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",-10.0,10.0),
                new ContinuousInterval("y",-10.0,10.0)
        );

        OptimizationTask optimizationTask = new PSOAlgorithm().getOptimizationTask();
        Map<String,Double> solution = optimizationTask.maximize(SIMPLE_FUNCTION,searchSpace);
        double x = solution.get("x");
        double y = solution.get("y");
        assertTrue(validatePredictedResult(0.707,x,0.05));
        assertTrue(validatePredictedResult(0.000,y,0.05));
    }



}