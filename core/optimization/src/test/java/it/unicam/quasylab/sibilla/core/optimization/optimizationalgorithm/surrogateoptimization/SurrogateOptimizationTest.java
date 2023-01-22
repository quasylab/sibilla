package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.surrogateoptimization;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategyFactory;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.ROSENBROCK_FUNCTION;
import static org.junit.jupiter.api.Assertions.*;

class SurrogateOptimizationTest {

    private boolean beingInRange(double value, double beginRange ,double endRange){
        return value >= beginRange && value <= endRange;
    }

    void minimizeFunctionFromFactory(){
        Function<Map<String,Double>,Double> functionToOptimize = (
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


        Properties properties = new Properties();
        properties.put("pso.particles.number","1000");
        properties.put("surrogate.optimization.training.set.size","100");
        OptimizationStrategy so = OptimizationStrategyFactory.getOptimizationStrategy("rfr","pso","lhs",100,functionToOptimize,null,searchSpace,properties);

    }
    @Test
    void minimizeFunction() {

        Function<Map<String,Double>,Double> functionToOptimize = (
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


        Properties properties = new Properties();
        properties.put("pso.particles.number","1000");
        properties.put("surrogate.optimization.training.set.size","100");
        SurrogateOptimization so = new SurrogateOptimization(functionToOptimize,null,searchSpace,properties);
        Map<String,Double> minimizingValues = so.minimize();
        boolean isLocalMinima1 = beingInRange(minimizingValues.get("x"),-1.1, -0.30) &&
                beingInRange(minimizingValues.get("y"),0.3, 1.1);

        boolean isLocalMinima2 = beingInRange(minimizingValues.get("x"),0.3, 1.1) &&
                beingInRange(minimizingValues.get("y"),-1.1, -0.3);

        assertTrue(isLocalMinima1 || isLocalMinima2);
    }

    @Test
    void minimize() {
        // FUNCTION TO BE OPTIMIZED
        // ROSENBROCK_FUNCTION

        // CONSTRAINTS
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();

        // SEARCH SPACE
        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x1",-5.0,5.0),
                new ContinuousInterval("x2",-5.0,5.0),
                new ContinuousInterval("x3",-5.0,5.0)
        );


        Properties properties = new Properties();
        properties.put("pso.particles.number","100");
        properties.put("surrogate.optimization.training.set.size","1000");
        SurrogateOptimization so = new SurrogateOptimization(ROSENBROCK_FUNCTION,constraints,searchSpace,properties);

        Map<String,Double> solutionFromSO =so.minimize();
        //System.out.println(so.minimize());

        OptimizationStrategy o = OptimizationStrategyFactory.getOptimizationStrategy("pso",ROSENBROCK_FUNCTION
        ,new ArrayList<>(),searchSpace,properties);

        Map<String,Double> solutionFromO = o.minimize();
        //System.out.println(o.minimize());

        Map<String,Double> real_optimal_solution = new HashMap<>();
        real_optimal_solution.put("x1",1.0);
        real_optimal_solution.put("x2",1.0);
        real_optimal_solution.put("x3",1.0);

        String results = "Rosenbrock function \n " +
                "True minima : "+
                "{x1=1.0, x2=1.0, x3=1.0} -> " + ROSENBROCK_FUNCTION.apply(real_optimal_solution)+"\n"+
                solutionFromSO + " -> "+ ROSENBROCK_FUNCTION.apply(solutionFromSO)+"\n"+
                solutionFromO + " -> "+ ROSENBROCK_FUNCTION.apply(solutionFromO);

        //System.out.println(results);
    }
//
//    @Test
//    void sibillaTestSIR() {
//
//        String CODE = """
//                param beta = 1.0;\s
//                param gamma = 1.0;\s
//                species S;\s
//                species I;\s
//                species R;\s
//                rule infection { S|I -[ #S*#I*beta ]-> I|I }
//                rule immunisation { I -[ #I*gamma ]-> R }
//                system init = S<95>|I<5>|R<0>;
//                predicate allRecovered = (#I ==0) ;""";
//
//
//        Function<Map<String,Double>,Double> sibillaFunction = map ->{
//            double result;
//            try {
//                SibillaRuntime sr = getRuntimeWithModule();
//                sr.load(CODE);
//                sr.setParameter("beta",map.get("beta"));
//                sr.setParameter("gamma",map.get("gamma"));
//                int time = map.get("timeUnits").intValue();
//                sr.setDeadline(time);//map.get("timeUnits"));
//                sr.setReplica(10);
//                sr.setDt(1.0);
//                sr.setConfiguration("init");
//                result = sr.computeProbReach(null,"allRecovered",0.1,0.01);
//                sr.reset();
//            } catch (CommandExecutionException e) {
//                throw new IllegalArgumentException("Something wrong is happening with Sibilla");
//            }
//            return result;
//        };
//
//        // CONSTRAINTS
//        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
//
//
//        constraints.add( map -> map.get("beta") >= 0.005 && map.get("beta") <=0.3);
//        constraints.add( map -> map.get("gamma") >= 0.005 && map.get("gamma") <=0.2);
//        constraints.add( map -> map.get("timeUnits") >= 100.0 && map.get("timeUnits") <=120.0);
//
//
//        // SEARCH SPACE
//        HyperRectangle searchSpace = new HyperRectangle(
//                new ContinuousInterval("beta",0.005,0.3),
//                new ContinuousInterval("gamma",0.005, 0.2),
//                new ContinuousInterval("timeUnits",100.0,120.0)
//        );
//        Properties properties = new Properties();
//
//        properties.put("pso.particles.number","100");
//        properties.put("surrogate.optimization.training.set.size","1000");
//        SurrogateOptimization so = new SurrogateOptimization(sibillaFunction,constraints,searchSpace,properties);
//        Map<String, Double> minimizingValues = so.minimize();
//        System.out.println("value that minimize : " + minimizingValues);
//        System.out.println("result : "+ sibillaFunction.apply(minimizingValues));
//
//
//    }
//
//    /**
//     * configuration (a)
//     * time --> varies in [ 100.0 , 120.0 ]
//     * beta --> varies in [ 0.005 , 0.3 ]
//     */
//    @Test
//    void testNetworkEpidemicsConfigurationA() throws CommandExecutionException {
//
//        String CODE_SIR = """
//                param beta = 1.0;\s
//                param gamma = 1.0;\s
//                species S;\s
//                species I;\s
//                species R;\s
//                rule infection { S|I -[ #S*#I*beta ]-> I|I }
//                rule immunisation { I -[ #I*gamma ]-> R }
//                system init = S<95>|I<5>|R<0>;
//                predicate allRecovered = (#I ==0) ;""";
//
//        SibillaRuntime sr = getRuntimeWithModule();
//        sr.load(CODE_SIR);
//
//        Function<Map<String,Double>,Double> reachabilityTerminationEpidemics = map ->{
//            double result;
//            try {
//                sr.setParameter("beta",map.get("beta"));
//                int time = map.get("timeUnits").intValue();
//                sr.setDeadline(time);
//                sr.setReplica(20);
//                sr.setDt(0.5);
//                sr.setConfiguration("init");
//                result = sr.computeProbReach(null,"allRecovered",0.1,0.01);
//                sr.reset();
//            } catch (CommandExecutionException e) {
//                throw new IllegalArgumentException("Something wrong is happening with Sibilla");
//            }
//            return result;
//        };
//
//        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
//
//        constraints.add( map -> map.get("beta") >= 0.005 && map.get("beta") <=0.3);
//        constraints.add( map -> map.get("timeUnits") >= 100.0 && map.get("timeUnits") <=120.0);
//
//        // SEARCH SPACE
//        HyperRectangle searchSpace = new HyperRectangle(
//                new ContinuousInterval("beta",0.005,0.3),
//                new ContinuousInterval("timeUnits",100.0,120.0)
//        );
//
//        Properties properties = new Properties();
//
//        properties.put("pso.particles.number","100");
//        properties.put("surrogate.optimization.training.set.size","1000");
//
//        SurrogateOptimization so = new SurrogateOptimization(
//                reachabilityTerminationEpidemics,
//                constraints,
//                searchSpace,
//                properties
//        );
//        Map<String, Double> minimizingValues = so.minimize();
//
////        OptimizationStrategy o = new ParticleSwarmOptimization(
////                reachabilityTerminationEpidemics,
////                constraints,
////                searchSpace,
////                properties
////        );
////        Map<String, Double> minimizingValues = o.minimize();
//
//        System.out.println("value that minimize : " + minimizingValues);
//        System.out.println("result : "+ reachabilityTerminationEpidemics.apply(minimizingValues));
//
//    }
//
//
//
//
//    private SibillaRuntime getRuntimeWithModule() throws CommandExecutionException {
//        SibillaRuntime sr = new SibillaRuntime();
//        sr.loadModule(PopulationModelModule.MODULE_NAME);
//        return sr;
//    }
}