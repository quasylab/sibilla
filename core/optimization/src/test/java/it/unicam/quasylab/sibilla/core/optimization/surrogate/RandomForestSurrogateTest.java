package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.RandomSampling;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Table;

import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"UnusedDeclaration"})
class RandomForestSurrogateTest {

    @Test
    void testTrainingSet(){

        Function<Map<String,Double>,Double> functionToLearn = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        Table sampleSet = new RandomSampling().getSampleTable(
                1000,
                    new HyperRectangle(
                            new Interval("x",-2.0,2.0),
                            new Interval("y",-2.0,2.0)
                    )
                );

        TrainingSet trainingSet = new TrainingSet(sampleSet,functionToLearn);
        RandomForestSurrogate rfr = new RandomForestSurrogate();
        rfr.fit(trainingSet);
        System.out.println(rfr.getSurrogateMetrics().toString());

    }

}