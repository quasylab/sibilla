package it.unicam.quasylab.sibilla.core.optimization.sampling;

import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.List;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_NO_SUCH_SAMPLING_STRATEGY;

public class SampleStrategyFactory {

    public static SamplingStrategy getSamplingStrategy(String samplingStrategyName){
        if(samplingStrategyName.equals("lhs"))
            return new LatinHyperCubeSampling();
        if(samplingStrategyName.equals("ffs"))
            return new FullFactorialSampling();
        if(samplingStrategyName.equals("rs"))
            return new RandomSampling();
        else
            throw new IllegalArgumentException(EXCEPT_NO_SUCH_SAMPLING_STRATEGY + " : "
                    + samplingStrategyName + "\n the available sampling strategies are: \n"+
                    getSamplingStrategiesNameList().stream().reduce("",(a,b)-> a + b + "\n"));
    }

    public static Table getSample(String samplingStrategyName,
                                  int numberOfSamples,
                                  HyperRectangle hyperRectangle){
        return getSamplingStrategy(samplingStrategyName).getSampleTable(numberOfSamples,hyperRectangle);
    }

    public static List<String> getSamplingStrategiesNameList(){
        return Arrays.stream(new String[]{"lhs","ffs","rs"}).toList();
    }
}
