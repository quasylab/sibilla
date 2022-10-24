package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import org.junit.jupiter.api.Test;
import static it.unicam.quasylab.sibilla.core.optimization.Constants.*;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
/**
 * Test for Random Forest Surrogate
 *
 * @author      Lorenzo Matteucci
 */
@SuppressWarnings({"UnusedDeclaration"})
class RandomForestSurrogateTest {

    @Test
    void testTrainingAndPredict(){
        Function<Map<String,Double>,Double> functionToLearn = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );
        TrainingSet trainingSet = new TrainingSet(
                new HyperRectangle(
                        new Interval("x",-2.0,2.0),
                        new Interval("y",-2.0,2.0)
                ),
                "lhs",
                1000,
                functionToLearn)
                ;
        RandomForestSurrogate rfr = new RandomForestSurrogate();
        rfr.fit(trainingSet);
        System.out.println("metrics 1 ");
        System.out.println(rfr.getInSampleMetrics().toString());

        Properties newProp = new Properties();
        newProp.put("surrogate.random.forest.trees","1000");
        newProp.put("surrogate.random.forest.depth","100");
        newProp.put("not.surrogate.properties","100");
        rfr.setProperties(newProp);
        rfr.fit(trainingSet);
        System.out.println("metrics 2 ");
        System.out.println(rfr.getInSampleMetrics().toString());
    }

    @Test
    void testTrainingSetDifferentProperties(){

        TrainingSet trainingSet = new TrainingSet(
                new HyperRectangle(
                        new Interval("x",-5.0,5.0),
                        new Interval("y",-5.0,5.0)
                ),
                "lhs",
                10000,
                EGG_HOLDER_FUNCTION)
                ;
        RandomForestSurrogate rfr = new RandomForestSurrogate();
        rfr.fit(trainingSet);
        System.out.println("metrics 1 ");
        System.out.println(rfr.getInSampleMetrics().toString());

        Properties newProp = new Properties();
        newProp.put("surrogate.random.forest.trees","1000");
        newProp.put("surrogate.random.forest.depth","100");
        newProp.put("not.surrogate.properties","100");
        rfr.setProperties(newProp);
        rfr.fit(trainingSet);
        System.out.println("metrics 2 ");
        System.out.println(rfr.getInSampleMetrics().toString());
    }




}