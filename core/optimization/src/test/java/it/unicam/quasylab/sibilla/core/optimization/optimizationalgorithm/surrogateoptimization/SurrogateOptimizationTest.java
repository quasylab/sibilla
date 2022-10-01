package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.surrogateoptimization;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategyFactory;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.TrainingSet;
import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.PopulationModelModule;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.ROSENBROCK_FUNCTION;
import static org.junit.jupiter.api.Assertions.*;

class SurrogateOptimizationTest {

    @Test
    void minimize() {
        // FUNCTION TO BE OPTIMIZED
        Function<Map<String,Double>,Double> functionToOptimize = ROSENBROCK_FUNCTION;

        // CONSTRAINTS
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();

        // SEARCH SPACE
        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("x1",-10.0,10.0),
                new Interval("x2",-10.0,10.0),
                new Interval("x3",-10.0,10.0)
        );

        Properties properties = new Properties();
        properties.put("particlesNumber","1000");
        properties.put("surrogate.optimization.training.set.size","10000");
        SurrogateOptimization so = new SurrogateOptimization(functionToOptimize,constraints,searchSpace,properties);
        System.out.println(so.minimize());
    }

    @Test
    void sibillaTest() throws CommandExecutionException {

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

        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE);

        Function<Map<String,Double>,Double> sibillaFunction = map ->{
            double result;
            try {
                sr.setParameter("beta",map.get("beta"));
                sr.setParameter("gamma",map.get("gamma"));
                sr.setDeadline(map.get("timeUnits"));
                sr.setReplica(100);
                sr.setDt(0.5);
                sr.setConfiguration("init");
                result = sr.computeProbReach(null,"allRecovered",0.1,0.1);
            } catch (CommandExecutionException e) {
                return 0.0;
            }
            return result;
        };

        // CONSTRAINTS
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();

        constraints.add( map -> map.get("beta") >= 0.005 && map.get("beta") <=0.3);
        constraints.add( map -> map.get("gamma") >= 0.005 && map.get("gamma") <=0.2);
        constraints.add( map -> map.get("timeUnits") >= 100.0 && map.get("gamma") <=120.0);

        // SEARCH SPACE
        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("beta",0.005,0.3),
                new Interval("gamma",0.005, 0.2),
                new Interval("timeUnits",100.0,120.0)
        );
        Properties properties = new Properties();
//        properties.put("particlesNumber","1000");
//        properties.put("surrogate.optimization.training.set.size","10000");

        OptimizationStrategy o = OptimizationStrategyFactory.getOConstrainedOptimizationStrategy("pso",sibillaFunction,constraints,searchSpace,properties);
        Map<String, Double> minimizingValues = o.minimize();
        System.out.println(minimizingValues);
        System.out.println(sibillaFunction.apply(minimizingValues));


//        properties.put("particlesNumber","1000");
//        properties.put("surrogate.optimization.training.set.size","10000");
//        SurrogateOptimization so = new SurrogateOptimization(sibillaFunction,constraints,searchSpace,properties);
//        Map<String, Double> minimizingValues = so.minimize();
//        System.out.println("value that minimize : " + minimizingValues);
//        System.out.println("result : "+ sibillaFunction.apply(minimizingValues));


    }

    private SibillaRuntime getRuntimeWithModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(PopulationModelModule.MODULE_NAME);
        return sr;
    }
}