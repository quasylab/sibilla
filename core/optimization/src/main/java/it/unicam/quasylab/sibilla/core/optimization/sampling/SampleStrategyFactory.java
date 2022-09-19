package it.unicam.quasylab.sibilla.core.optimization.sampling;

import tech.tablesaw.api.Table;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_NO_SUCH_SAMPLING_STRATEGY;

public class SampleStrategyFactory {

    public static SamplingStrategy SampleStrategy(String samplingStrategyName){
        if(samplingStrategyName.equals("lhs"))
            return new LatinHyperCubeSampling();
        if(samplingStrategyName.equals("ffs"))
            return new FullFactorialSampling();
        if(samplingStrategyName.equals("rs"))
            return new RandomSampling();
        else
            throw new IllegalArgumentException(EXCEPT_NO_SUCH_SAMPLING_STRATEGY + " : "+ samplingStrategyName);
    }

    public static Table getSample(String samplingStrategyName,
                                  int numberOfSamples,
                                  HyperRectangle hyperRectangle){
        if(samplingStrategyName.equals("lhs"))
            return new LatinHyperCubeSampling().getSampleTable(numberOfSamples,hyperRectangle);
        if(samplingStrategyName.equals("ffs"))
            return new FullFactorialSampling().getSampleTable(numberOfSamples,hyperRectangle);
        if(samplingStrategyName.equals("rs"))
            return new RandomSampling().getSampleTable(numberOfSamples,hyperRectangle);
        else
            throw new IllegalArgumentException(EXCEPT_NO_SUCH_SAMPLING_STRATEGY + " : "+ samplingStrategyName);
    }

    public static String[] getSamplingStrategiesNameList(){
        return new String[]{"lhs","ffs","rs"};
    }
}
