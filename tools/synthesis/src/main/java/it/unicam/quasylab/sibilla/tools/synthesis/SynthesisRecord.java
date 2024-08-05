package it.unicam.quasylab.sibilla.tools.synthesis;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.DataSet;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateMetrics;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public record SynthesisRecord(
        String chosenOptimizationAlgorithm,
        String chosenSurrogateModel,
        String chosenSamplingStrategy,
        ToDoubleFunction<Map<String,Double>> objectiveFunction,
        ToDoubleFunction<Map<String,Double>> surrogateFunction,
        DataSet realFunDataset,
        boolean useSurrogate,
        boolean isMinimizationProblem,
        HyperRectangle searchSpace,
        Properties properties,
        int numberOfSamples,
        double trainingPortion,
        List<Predicate<Map<String, Double>>> constraints,
        Map<String,Double> optimalCoordinates,
        double optimalValueObjectiveFunction,
        double optimalValueSurrogateFunction,
        SurrogateMetrics inSampleMetrics,
        SurrogateMetrics outOfSampleMetrics
) {

    public List<Map<String,Double>> getDatasetAsMapList(){
        return realFunDataset.toListOfMaps();
    }
    public String info(boolean verbose){
        StringBuilder sb = new StringBuilder();


        sb.append("Optimal Coordinates: ").append(optimalCoordinates != null ? optimalCoordinates : "not present").append("\n");
        sb.append("Optimal Value (Objective Function): ").append(optimalValueObjectiveFunction).append("\n");
        sb.append("Optimal Value (Surrogate Function): ").append(optimalValueSurrogateFunction).append("\n");

        if (verbose) {
            sb.append("Optimization Algorithm: ").append(chosenOptimizationAlgorithm != null ? chosenOptimizationAlgorithm : "not present").append("\n");
            sb.append("Surrogate Model: ").append(chosenSurrogateModel != null ? chosenSurrogateModel : "not present").append("\n");
            sb.append("Sampling Strategy: ").append(chosenSamplingStrategy != null ? chosenSamplingStrategy : "not present").append("\n");
            sb.append("Use Surrogate: ").append(useSurrogate).append("\n");
            sb.append("Is Minimization Problem: ").append(isMinimizationProblem).append("\n");
            sb.append("Number of Samples: ").append(numberOfSamples).append("\n");
            sb.append("Training Portion: ").append(trainingPortion).append("\n");
            sb.append("Dataset: ").append(realFunDataset != null ? realFunDataset : "not present").append("\n");
            sb.append("Search Space: ").append(searchSpace != null ? searchSpace : "not present").append("\n");
            sb.append("Properties: ").append(properties != null ? properties : "not present").append("\n");
            sb.append("In-Sample Metrics: ").append(inSampleMetrics != null ? inSampleMetrics : "not present").append("\n");
            sb.append("Out-of-Sample Metrics: ").append(outOfSampleMetrics != null ? outOfSampleMetrics : "not present").append("\n");
        }

        return sb.toString();
    }


}