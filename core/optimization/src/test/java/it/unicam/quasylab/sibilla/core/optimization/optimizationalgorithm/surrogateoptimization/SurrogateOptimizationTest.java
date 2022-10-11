package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.surrogateoptimization;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategyFactory;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.PopulationModelModule;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
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
                new Interval("x",-2.0,2.0),
                new Interval("y",-2.0,2.0)
        );


        Properties properties = new Properties();
        properties.put("pso.particles.number","1000");
        properties.put("surrogate.optimization.training.set.size","100");
        SurrogateOptimization so = new SurrogateOptimization(functionToOptimize,null,searchSpace,properties);
        Map<String,Double> minimizingValues = so.minimize();
        boolean isLocalMinima1 = beingInRange(minimizingValues.get("x"),-0.95, -0.55) &&
                beingInRange(minimizingValues.get("y"),0.55, 0.95);

        boolean isLocalMinima2 = beingInRange(minimizingValues.get("x"),0.55, 0.95) &&
                beingInRange(minimizingValues.get("y"),-0.95, -0.55);

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
                new Interval("x1",-5.0,5.0),
                new Interval("x2",-5.0,5.0),
                new Interval("x3",-5.0,5.0)
        );


        Properties properties = new Properties();
        properties.put("pso.particles.number","100");
        properties.put("surrogate.optimization.training.set.size","1000");
        SurrogateOptimization so = new SurrogateOptimization(ROSENBROCK_FUNCTION,constraints,searchSpace,properties);

        Map<String,Double> solutionFromSO =so.minimize();
        //System.out.println(so.minimize());

        OptimizationStrategy o = OptimizationStrategyFactory.getOConstrainedOptimizationStrategy("pso",ROSENBROCK_FUNCTION
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

        System.out.println(results);
    }

    @Test
    void sibillaTestSIR() {

        String CODE = """
                param beta = 1.0;\s
                param gamma = 1.0;\s
                species S;\s
                species I;\s
                species R;\s
                rule infection { S|I -[ #S*#I*beta ]-> I|I }
                rule immunisation { I -[ #I*gamma ]-> R }
                system init = S<95>|I<5>|R<0>;
                predicate allRecovered = (#I ==0) ;""";


        Function<Map<String,Double>,Double> sibillaFunction = map ->{
            double result;
            try {
                SibillaRuntime sr = getRuntimeWithModule();
                sr.load(CODE);
                sr.setParameter("beta",map.get("beta"));
                sr.setParameter("gamma",map.get("gamma"));
                int time = map.get("timeUnits").intValue();
                sr.setDeadline(time);//map.get("timeUnits"));
                sr.setReplica(10);
                sr.setDt(1.0);
                sr.setConfiguration("init");
                result = sr.computeProbReach(null,"allRecovered",0.1,0.01);
                sr.reset();
            } catch (CommandExecutionException e) {
                throw new IllegalArgumentException("Something wrong is happening with Sibilla");
            }
            return result;
        };

        // CONSTRAINTS
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();


        constraints.add( map -> map.get("beta") >= 0.005 && map.get("beta") <=0.3);
        constraints.add( map -> map.get("gamma") >= 0.005 && map.get("gamma") <=0.2);
        constraints.add( map -> map.get("timeUnits") >= 100.0 && map.get("timeUnits") <=120.0);


        // SEARCH SPACE
        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("beta",0.005,0.3),
                new Interval("gamma",0.005, 0.2),
                new Interval("timeUnits",100.0,120.0)
        );
        Properties properties = new Properties();

        properties.put("pso.particles.number","100");
        properties.put("surrogate.optimization.training.set.size","1000");
        SurrogateOptimization so = new SurrogateOptimization(sibillaFunction,constraints,searchSpace,properties);
        Map<String, Double> minimizingValues = so.minimize();
        System.out.println("value that minimize : " + minimizingValues);
        System.out.println("result : "+ sibillaFunction.apply(minimizingValues));


    }

    @Test
    void testSibillaProkaryoticGeneExpression(){
        String CODE = """
                param beta = 1.0;\s
                param gamma = 1.0;\s
                species S;\s
                species I;\s
                species R;\s
                rule infection { S|I -[ #S*#I*beta ]-> I|I }
                rule immunisation { I -[ #I*gamma ]-> R }
                system init = S<95>|I<5>|R<0>;
                predicate allRecovered = (#I ==0) ;""";
    }




    private SibillaRuntime getRuntimeWithModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(PopulationModelModule.MODULE_NAME);
        return sr;
    }
}