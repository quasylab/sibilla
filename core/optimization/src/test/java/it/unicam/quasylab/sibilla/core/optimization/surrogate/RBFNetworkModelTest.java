package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;
@Disabled
class RBFNetworkModelTest {

    @Test
    void testNet(){

        //sin(5x)*cos(5y)/5
        ToDoubleFunction<Map<String,Double>> funToOpt = map -> Math.sin(5*map.get("x")) *Math.cos(5*map.get("y"))/5;

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",-2.0,2.0),
                new ContinuousInterval("y",-2.0,2.0)
        );

        DataSet ts = new DataSet(searchSpace,new LatinHyperCubeSamplingTask(),10000,funToOpt);

        RBFNetworkModel rbfNetworkSurrogate = new RBFNetworkModel(ts,0.85,new Properties());

        rbfNetworkSurrogate.fit();


        double res = rbfNetworkSurrogate.predict(new Double[]{1.0,1.0});

        System.out.println(res);
    }

}