package it.unicam.quasylab.sibilla.core.optimization;


import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.SampleStrategyFactory;
import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingStrategy;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.Surrogate;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.TrainingSet;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *  could be useful here make a mechanism to re-train
 *  the surrogate in case minimum don't converge
 */
public class SurrogateOptimization implements OptimizationStrategy{

    private String surrogateName;
    private String optimizationName;
    private Function<Map<String,Double>,Double> functionToBeSurrogate;
    private List<Predicate<Map<String,Double>>> constraints;
    private HyperRectangle searchSpace;
    private Properties surOptProperties;

    private TrainingSetEvalCriteria trainingSetEvalCriteria;
    private int trainingSetSize = 1000;



    public SurrogateOptimization(String surrogateName,
                                 String optimizationName,
                                 Function<Map<String,Double>,Double> functionToBeSurrogate,
                                 List<Predicate<Map<String,Double>>> constraints,
                                 HyperRectangle searchSpace,
                                 Properties surOptProperties){
        this.surrogateName = surrogateName;
        this.optimizationName = optimizationName;
        this.functionToBeSurrogate = functionToBeSurrogate;
        this.constraints =constraints;
        this.searchSpace = searchSpace;
        this.surOptProperties = surOptProperties;

        this.trainingSetEvalCriteria = ts -> ts.getResultSD() != 0;
        this.trainingSetSize = 1000;
    }


    private TrainingSet generateTrainingSet(String samplingStrategyName,
                                            int trainingSetSize,
                                            HyperRectangle searchSpace,
                                            Function<Map<String,Double>,Double> functionToBeSurrogate){
        Table sampleSet = SampleStrategyFactory
                .SampleStrategy(samplingStrategyName)
                .getSampleTable(trainingSetSize,searchSpace);
        return   new TrainingSet(sampleSet, functionToBeSurrogate);
    }


    private void evaluateTrainingSet(TrainingSet ts){
        if(!trainingSetEvalCriteria.eval(ts))
            System.out.println(); //TODO
    }


    public void setTrainingSetEvalCriteria(TrainingSetEvalCriteria trainingSetEvalCriteria) {
        this.trainingSetEvalCriteria = trainingSetEvalCriteria;
    }


    @Override
    public Map<String, Double> minimize() {
        return null;
    }

    @Override
    public Map<String, Double> maximize() {
        return null;
    }
}
