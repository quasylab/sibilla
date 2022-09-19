package it.unicam.quasylab.sibilla.core.optimization.blackboxoptimization.pso;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.ParticleSwarmOptimization;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;


class ParticleSwarmOptimizationTest {

    private boolean beingInRange(double value, double beginRange ,double endRange){
        return value >= beginRange && value <= endRange;
    }
    /**
     * simple function : <b> 7 * ( x * y )/(e^(x^2+y^2)) </b> minimization
     *
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

        Function<Map<String,Double>,Double> functionToOptimize_1 = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        HyperRectangle searchSpace_1 = new HyperRectangle(
                new Interval("x",-2.0,2.0),
                new Interval("y",-2.0,2.0)
        );

        int psoIteration = 500;
        int numParticles = 500;

        Map<String,Double> minimizingValues =
                new ParticleSwarmOptimization(
                        functionToOptimize_1,
                        searchSpace_1,
                        psoIteration,
                        numParticles )
                        .minimize();

        boolean isLocalMinima1 = beingInRange(minimizingValues.get("x"),-0.95, -0.55) &&
                beingInRange(minimizingValues.get("y"),0.55, 0.95);

        boolean isLocalMinima2 = beingInRange(minimizingValues.get("x"),0.55, 0.95) &&
                beingInRange(minimizingValues.get("y"),-0.95, -0.55);

        assertTrue(isLocalMinima1 || isLocalMinima2);
    }

    @Test
    void maximizeFunction() {

        Function<Map<String,Double>,Double> functionToOptimize_1 = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        HyperRectangle searchSpace_1 = new HyperRectangle(
                new Interval("x",-2.0,2.0),
                new Interval("y",-2.0,2.0)
        );

        int psoIteration = 500;
        int numParticles = 500;

        Map<String,Double> maximizingValues =
                new ParticleSwarmOptimization(
                        functionToOptimize_1,
                        searchSpace_1,
                        psoIteration,
                        numParticles )
                        .maximize();

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
    @Disabled("Disabled : very time consuming")
    void minimizeFunctionWithConstraint() {

        Function<Map<String,Double>,Double> functionToOptimize = (
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

        HyperRectangle searchSpace_1 = new HyperRectangle(
                new Interval("x1",-10.0,10.0),
                new Interval("x2",-10.0,10.0)
        );

        int psoIteration = 1000;
        int numParticles = 1000;

        Map<String,Double> minimizingValues =
                new ParticleSwarmOptimization(
                        functionToOptimize,
                        constraints,
                        searchSpace_1,
                        psoIteration,
                        numParticles )
                        .minimize();

        System.out.println(minimizingValues.get("x1"));
        System.out.println(minimizingValues.get("x2"));
        System.out.println("---");
        System.out.println(functionToOptimize.apply(minimizingValues));

    }

    @Disabled("Disabled : very time consuming")
    @Test
    void minimizeMultidimensionalFunctionWithConstraint() {

        Function<Map<String,Double>,Double> functionToOptimize = (
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

        HyperRectangle searchSpace_1 = new HyperRectangle(
                new Interval("x1",-10.0,10.0),
                new Interval("x2",-10.0,10.0),
                new Interval("x3",-10.0,10.0),
                new Interval("x4",-10.0,10.0),
                new Interval("x5",-10.0,10.0),
                new Interval("x6",-10.0,10.0),
                new Interval("x7",-10.0,10.0)
        );

        int psoIteration = 100;
        int numParticles = 10000;

        Map<String,Double> minimizingValues =
                new ParticleSwarmOptimization(
                        functionToOptimize,
                        constraints,
                        searchSpace_1,
                        psoIteration,
                        numParticles )
                        .minimize();

        System.out.println(minimizingValues.get("x1"));
        System.out.println(minimizingValues.get("x2"));
        System.out.println(minimizingValues.get("x3"));
        System.out.println(minimizingValues.get("x4"));
        System.out.println(minimizingValues.get("x5"));
        System.out.println(minimizingValues.get("x6"));
        System.out.println(minimizingValues.get("x7"));
        System.out.println("--- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---");
        System.out.println(functionToOptimize.apply(minimizingValues));
        System.out.println("--- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---");

    }


    /**
     * In mathematical optimization, the Rosenbrock function is a non-convex function,
     * introduced by Howard H. Rosenbrock in 1960, which is used as a performance test
     * problem for optimization algorithms.
     *
     * <b> MINIMA : </b> has exactly one minimum for N=3 (at (1, 1, 1)) and
     * exactly two minima for 4 <= N <= 7 the global minimum of all ones
     * and a local minimum near (-1,1, ... ,1).
     *
     * @see    <a href=https://en.wikipedia.org/wiki/Rosenbrock_function">Rosenbrock_function</a>
     */

    @Disabled("Disabled : very time consuming")
    @Test
    public void testRosenbrockFunction(){

        Function<Map<String,Double>,Double> functionToOptimize = (
                map -> {
                    String[] keyList = map.keySet().toArray(new String[0]);
                    double sum = 0.0;
                    for (int i = 0; i < keyList.length - 1 ; i++){
                        sum += 100 * Math.pow(map.get(keyList[i+1]) - Math.pow(map.get(keyList[i]),2),2)+Math.pow((1-map.get(keyList[i])),2);
                    }
                    return sum;
                }
        );

        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("x1",-10.0,10.0),
                new Interval("x2",-10.0,10.0),
                new Interval("x3",-10.0,10.0)
        );

        Properties psoProperties = new Properties();
        psoProperties.setProperty("iteration", "1000");
        psoProperties.setProperty("particlesNumber", "10000");
        ParticleSwarmOptimization pso = new ParticleSwarmOptimization(functionToOptimize, searchSpace, psoProperties);

        pso.setSearchSpaceAsConstraints();

        Map<String,Double> minimizingValues = pso.minimize();
        System.out.println(minimizingValues.toString());
    }

    @Test
    @Disabled("Disabled : very time consuming")
    public void testCorrectFunctioningOfConstraints(){

        Function<Map<String,Double>,Double> functionToOptimize = map -> Math.cos(map.get("x")) * Math.cos(map.get("y"));

        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("x",-100.0,100.0),
                new Interval("y",-100.0,100.0)
        );

        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();


        constraints.add(
                map -> {
                    boolean x1 = 2 <= map.get("x") && map.get("x") <= 4;
                    boolean x2 = -2 <= map.get("y") && map.get("y") <= 2;
                    return x1 && x2;
                }
        );

        Properties psoProperties = new Properties();
        psoProperties.setProperty("iteration", "1000");
        psoProperties.setProperty("particlesNumber", "10000");
        ParticleSwarmOptimization pso = new ParticleSwarmOptimization(functionToOptimize,constraints, searchSpace, psoProperties);

        //pso.setSearchSpaceAsConstraints();

        Map<String,Double> minimizingValues = pso.minimize();
        System.out.println(minimizingValues.toString());
    }

    @Test
    public void testSetSearchSpaceAsConstraints(){

        Function<Map<String,Double>,Double> functionToOptimize = map -> Math.cos(map.get("x")) * Math.cos(map.get("y"));

        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("x",0.15,3.0)
        );

        Properties psoProperties = new Properties();
        psoProperties.setProperty("iteration", "1000");
        psoProperties.setProperty("particlesNumber", "10000");
        ParticleSwarmOptimization pso = new ParticleSwarmOptimization(functionToOptimize, searchSpace, psoProperties);

    }



}