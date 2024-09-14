package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrthoMADSTaskTest {


    //@Disabled
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

        Map<String,Double> minimizingValues = new OrthoMADSTask().minimize(functionToOptimize,searchSpace,new ArrayList<>(),new Properties(),123L);

        System.out.println(minimizingValues);

        double value = 1 / Math.sqrt(2);

        assertEquals(-value, minimizingValues.get("x"),0.1);
        assertEquals(value, minimizingValues.get("y"),0.1);

    }


}
