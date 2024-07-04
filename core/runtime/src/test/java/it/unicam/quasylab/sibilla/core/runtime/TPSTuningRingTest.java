package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.RandomForest;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModel;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.Signal;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import it.unicam.quasylab.sibilla.tools.stl.StlLoader;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.stl.StlMonitorFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

public class TPSTuningRingTest {

    private int numberOfTrajectories;
    private SimulationEnvironment se;
    private RandomGenerator rg;


    private String getFormulaStability(){

//        return """
//                measure %RED
//                measure %BLUE
//                formula formula_stability [] : \\E[0,25][ %RED >= 1.0 ]  || \\E[0,25][ %BLUE >= 1.0 ]   endformula
//                """;
        return """
                measure %RED
                measure %BLUE
                formula formula_stability [] : \\E[0,25][ %RED >= 1.0 ]   endformula
                """;

    }

    private String getFormulaCoherence(){
        return """
                measure %RED
                measure %BLUE
                formula formula_coherence [] : ([%RED> 3 * %BLUE]&&(\\E[0,25][%BLUE<=0.0])) endformula
                """;
    }

    private String getFormulaRedPresence(){
        return """
                measure %RED
                measure %BLUE
                formula formula_red [] : (\\G[0,25][%RED >= 0.5]) endformula
                """;
    }

    private String getFormulaCoherenceOLD(){
        return """
                measure %RED
                measure %BLUE
                formula formula_coherence [] : ([%RED> 3 * %BLUE]&&(\\E[0,25][%RED>=1.0])) endformula
                """;
    }

    /**
     * TSP base model
     */
    private String getTSPBaseString(){
        return """
                species RED;
                species BLUE;
                species UNC;
                                
                const meetRate = 1.0;
                                
                rule UNC_to_BLUE {
                    UNC|BLUE -[ #UNC * meetRate * %BLUE ]-> BLUE|BLUE
                }
                                
                rule UNC_to_RED {
                    UNC|RED -[ #UNC * meetRate * %RED ]-> RED|RED
                }
                                
                rule BLUE_to_UNC {
                    BLUE|RED -[ #BLUE * meetRate * %RED ]-> UNC|RED
                }
                                
                rule RED_to_UNC {
                    RED|BLUE -[ #RED * meetRate * %BLUE ]-> UNC|BLUE
                }
                                
                measure redPercentage = %RED;
                measure bluePercentage = %BLUE;
                measure uncPercentage = %UNC;
                                
                measure redQuantity = #RED;
                measure blueQuantity  = #BLUE;
                measure uncQuantity  = #UNC;
                                
                                
                param scale = 100.0;
                                
                system configuration_common = RED<100>|BLUE<100>|UNC<800>;
                                
                system configuration_1 = RED<1*scale>|BLUE<1*scale>|UNC<8*scale>;
                system configuration_2 = RED<100>|BLUE<100>|UNC<100>;
                """;
    }
    /**
     * TSP compartment model
     */
    private String getTspRingStringComplex(){
        return """
                  const K = 5;       /* length of the ring */
                  
                  species RED of [0,K];
                  species BLUE of [0,K];
                  species UNC of [0,K];
                  
                  param os_rate = 1.0; /* on site persuasion rate */
                  param a_rate = 1.0;  /* adjacent persuasion rate */
                  
                  rule UNC_to_BLUE_on_site for i in [0,K] {
                    UNC[i]|BLUE[i] -[  #UNC[i] * os_rate * %BLUE[i] ]-> BLUE[i]|BLUE[i]
                  }
                  
                  rule UNC_to_RED_on_site for i in [0,K] {
                    UNC[i]|RED[i] -[ #UNC[i] * os_rate * %RED[i] ]-> RED[i]|RED[i]
                  }
                  

                  rule UNC_to_BLUE_next for i in [0,K] {
                    UNC[(i+1)%K]|BLUE[i] -[ #UNC[(i+1)%K] * a_rate * %BLUE[i] ]-> BLUE[(i+1)%K]|BLUE[i]
                  }
                  
                  rule UNC_to_BLUE_prev for i in [0,K] {
                    UNC[(i-1+K)%K]|BLUE[i] -[ #UNC[(i-1+K)%K] * a_rate * %BLUE[i]  ]-> BLUE[(i-1+K)%K]|BLUE[i]
                  }
                  
                  
                  rule UNC_to_RED_next for i in [0,K] {
                    UNC[(i+1)%K]|RED[i] -[ #UNC[(i+1)%K] * a_rate * %RED[i] ]-> RED[(i+1)%K]|RED[i]
                  }
                  
                  rule UNC_to_RED_prev for i in [0,K] {
                    UNC[(i-1+K)%K]|RED[i] -[ #UNC[(i-1+K)%K] * a_rate * %RED[i]  ]-> RED[(i-1+K)%K]|RED[i]
                  }
                  
                  
                  rule BLUE_to_UNC_on_site for i in [0,K] {
                    BLUE[i]|RED[i] -[ #BLUE[i] * os_rate * %RED[i] ]-> UNC[i]|RED[i]
                  }
                  
                  rule RED_to_UNC_on_site for i in [0,K] {
                    RED[i]|BLUE[i] -[ #RED[i] * os_rate * %BLUE[i] ]-> UNC[i]|BLUE[i]
                  } 
                  
                  
                  
                  rule BLUE_to_UNC_next for i in [0,K] {
                    BLUE[i]|RED[(i+1)%K] -[ #BLUE[i] * a_rate * %RED[(i+1)%K]  ]-> UNC[i]|RED[(i+1)%K]
                  }
                  
                  rule BLUE_to_UNC_prev for i in [0,K] {
                    BLUE[i]|RED[(i-1+K)%K] -[#BLUE[i] * a_rate * %RED[(i-1+K)%K] ]-> UNC[i]|RED[(i-1+K)%K]
                  }
                  
                  
                  
                  rule RED_to_UNC_next for i in [0,K] {
                    RED[i]|BLUE[(i+1)%K] -[ #RED[i] * a_rate * %BLUE[(i+1)%K] ]-> UNC[i]|BLUE[(i+1)%K]
                  }
                  
                  rule RED_to_UNC_prev for i in [0,K] {
                    RED[i]|BLUE[(i-1+K)%K] -[ #RED[i] * a_rate * %BLUE[(i-1+K)%K] ]-> UNC[i]|BLUE[(i-1+K)%K]
                  }
                  
                  measure redPercentage = %RED[0] + %RED[1] + %RED[2] + %RED[3] + %RED[4];
                  measure bluePercentage = %BLUE[0] + %BLUE[1] + %BLUE[2] + %BLUE[3] + %BLUE[4];
                  measure uncPercentage = %UNC[0] + %UNC[1] + %UNC[2] + %UNC[3] + %UNC[4];
                  
                  measure redQuantity = #RED[0] + #RED[1] + #RED[2] + #RED[3] + #RED[4];
                  measure blueQuantity = #BLUE[0] + #BLUE[1] + #BLUE[2] + #BLUE[3] + #BLUE[4];
                  measure uncQuantity = #UNC[0] + #UNC[1] + #UNC[2] + #UNC[3] + #UNC[4];
                  
                          
                """;
    }

    private String getTspRingString(){
        return """
                  const K = 5;       /* length of the ring */
                  
                  species RED of [0,K];
                  species BLUE of [0,K];
                  species UNC of [0,K];
                  
                  param os_rate = 1.0; /* on site persuasion rate */
                  param a_rate = 1.0;  /* adjacent persuasion rate */
                  
                  rule UNC_to_BLUE_on_site for i in [0,K] {
                    UNC[i]|BLUE[i] -[  #UNC[i] * os_rate * %BLUE[i] ]-> BLUE[i]|BLUE[i]
                  }
                  
                  rule UNC_to_RED_on_site for i in [0,K] {
                    UNC[i]|RED[i] -[ #UNC[i] * os_rate * %RED[i] ]-> RED[i]|RED[i]
                  }
                  

                  rule UNC_to_BLUE_next for i in [0,K] {
                    UNC[(i+1)%K]|BLUE[i] -[ #UNC[(i+1)%K] * a_rate * %BLUE[i] ]-> BLUE[(i+1)%K]|BLUE[i]
                  }
                  
                  rule UNC_to_BLUE_prev for i in [0,K] {
                    UNC[(i-1+K)%K]|BLUE[i] -[ #UNC[(i-1+K)%K] * a_rate * %BLUE[i]  ]-> BLUE[(i-1+K)%K]|BLUE[i]
                  }
                  
                  
                  rule UNC_to_RED_next for i in [0,K] {
                    UNC[(i+1)%K]|RED[i] -[ #UNC[(i+1)%K] * a_rate * %RED[i] ]-> RED[(i+1)%K]|RED[i]
                  }
                  
                  rule UNC_to_RED_prev for i in [0,K] {
                    UNC[(i-1+K)%K]|RED[i] -[ #UNC[(i-1+K)%K] * a_rate * %RED[i]  ]-> RED[(i-1+K)%K]|RED[i]
                  }
                  
                  
                  rule BLUE_to_UNC_on_site for i in [0,K] {
                    BLUE[i]|RED[i] -[ #BLUE[i] * os_rate * %RED[i] ]-> UNC[i]|RED[i]
                  }
                  
                  rule RED_to_UNC_on_site for i in [0,K] {
                    RED[i]|BLUE[i] -[ #RED[i] * os_rate * %BLUE[i] ]-> UNC[i]|BLUE[i]
                  } 
                  
                  
                  
                  rule BLUE_to_UNC_next for i in [0,K] {
                    BLUE[i]|RED[(i+1)%K] -[ #BLUE[i] * a_rate * %RED[(i+1)%K]  ]-> UNC[i]|RED[(i+1)%K]
                  }
                  
                  rule BLUE_to_UNC_prev for i in [0,K] {
                    BLUE[i]|RED[(i-1+K)%K] -[#BLUE[i] * a_rate * %RED[(i-1+K)%K] ]-> UNC[i]|RED[(i-1+K)%K]
                  }
                  
                  
                  
                  rule RED_to_UNC_next for i in [0,K] {
                    RED[i]|BLUE[(i+1)%K] -[ #RED[i] * a_rate * %BLUE[(i+1)%K] ]-> UNC[i]|BLUE[(i+1)%K]
                  }
                  
                  rule RED_to_UNC_prev for i in [0,K] {
                    RED[i]|BLUE[(i-1+K)%K] -[ #RED[i] * a_rate * %BLUE[(i-1+K)%K] ]-> UNC[i]|BLUE[(i-1+K)%K]
                  }
                  
                  measure redPercentage = %RED[0] + %RED[1] + %RED[2] + %RED[3] + %RED[4];
                  measure bluePercentage = %BLUE[0] + %BLUE[1] + %BLUE[2] + %BLUE[3] + %BLUE[4];
                  measure uncPercentage = %UNC[0] + %UNC[1] + %UNC[2] + %UNC[3] + %UNC[4];
                  
                  measure redQuantity = #RED[0] + #RED[1] + #RED[2] + #RED[3] + #RED[4];
                  measure blueQuantity = #BLUE[0] + #BLUE[1] + #BLUE[2] + #BLUE[3] + #BLUE[4];
                  measure uncQuantity = #UNC[0] + #UNC[1] + #UNC[2] + #UNC[3] + #UNC[4];
                  
                          
                """;
    }
    private Map<String, ToDoubleFunction<PopulationState>> getMeasureMapTSPBase(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSP = new HashMap<>();
        measuresMappingTSP.put("%RED", s -> s.getFraction(0));
        measuresMappingTSP.put("%BLUE", s -> s.getFraction(1));
        return measuresMappingTSP;
    }
    private Map<String, ToDoubleFunction<PopulationState>> getMeasureMapTSPRing(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSP = new HashMap<>();
        measuresMappingTSP.put("%RED", s -> s.getFraction(0,1,2,3,4));
        measuresMappingTSP.put("%BLUE", s -> s.getFraction(5,6,7,8,9));
        return measuresMappingTSP;
    }
    private PopulationModelDefinition getPopulationModelDefinition(String modelSpecification) {
        PopulationModelGenerator pmg =new PopulationModelGenerator(modelSpecification);
        try {
            return pmg.getPopulationModelDefinition();
        } catch (ModelGenerationException e) {
            throw new RuntimeException(e);
        }
    }
    private PopulationModel getPopulationModel(String modelSpecification){
        return getPopulationModelDefinition(modelSpecification).createModel();
    }
    private QuantitativeMonitor<PopulationState> getQuantitativeMonitor(String formulaDefinition, String formulaName, double[] formulaParameter, Map<String, ToDoubleFunction<PopulationState>> measureMapFunction) throws StlModelGenerationException {
        StlLoader loader = new StlLoader(formulaDefinition);
        StlMonitorFactory<PopulationState> monitorFactory = loader.getModelFactory(measureMapFunction);
        return monitorFactory.getQuantitativeMonitor(formulaName, formulaParameter);
    }
    private BiFunction<double[],double[],Double> euclideanDistance(){
        return (vec1,vec2) ->{
            if (vec1.length != vec2.length) throw new IllegalArgumentException("Vectors must have the same length");
            double sumOfSquares = 0;
            for (int i = 0; i < vec1.length; i++) {
                double difference = vec1[i] - vec2[i];
                sumOfSquares += difference * difference;
            }
            return Math.sqrt(sumOfSquares);
        };
    }


    // Chebyshev Distance
    private BiFunction<double[], double[], Double> chebyshevDistance() {
        return (vec1, vec2) -> {
            if (vec1.length != vec2.length) throw new IllegalArgumentException("Vectors must have the same length");
            return IntStream.range(0, vec1.length)
                    .mapToDouble(i -> Math.abs(vec1[i] - vec2[i]))
                    .max()
                    .orElseThrow(() -> new IllegalArgumentException("Vectors must not be empty"));
        };
    }
    private BiFunction<double[],double[],Double> euclideanDistanceInMaximization(){
        return (vec1,vec2) ->{
            if (vec1.length != vec2.length) throw new IllegalArgumentException("Vectors must have the same length");
            double sumOfSquares = 0;
            for (int i = 0; i < vec1.length; i++) {
                double difference = Math.max(0,vec1[i] - vec2[i]);
                sumOfSquares += difference * difference;
            }
            return Math.sqrt(sumOfSquares);
        };
    }
    private Trajectory<PopulationState> sampleATrajectory(PopulationModel currentModel, PopulationState currentInitialState, double timeHorizon){
        Trajectory<PopulationState> trajectory= se.sampleTrajectory(
                rg,
                currentModel,
                currentInitialState,
                timeHorizon);
        trajectory.setEnd(timeHorizon);
        return trajectory;
    }
    private double robustness(PopulationModel currentModel, PopulationState currentInitialState, QuantitativeMonitor<PopulationState> currentMonitor){
        double robustness = 0.0;
        for (int i = 0; i < this.numberOfTrajectories; i++) {
            Trajectory<PopulationState> trj = this.sampleATrajectory(currentModel,
                    currentInitialState,
                    currentMonitor.getTimeHorizon());
            Signal robustnessSignal = currentMonitor.monitor(trj);
            double currentRobustness = robustnessSignal.valueAt(0.0);
            robustness += currentRobustness;

        }
        return robustness / this.numberOfTrajectories;

    }

    private PopulationState getBaseInitialState(){
        return new PopulationState(new int[]{
                12,  // RED
                8, // BLUE
                80  // UNC
        });
    }

    private PopulationState getInitialRingState(Map<String,Double> param){
        int n = 20;
        int k = 5;
        ToIntFunction<Double> redFromFrac= fraction -> Math.toIntExact(Math.round(fraction * n / k));
        ToIntFunction<Double> blueFromFrac = fraction -> Math.toIntExact(Math.round((1-fraction) * n / k));


        return new PopulationState(new int[]{
                redFromFrac.applyAsInt(param.get("redFracIn0")),   //  0 - RED[0]
                redFromFrac.applyAsInt(param.get("redFracIn1")),   //  1 - RED[1]
                redFromFrac.applyAsInt(param.get("redFracIn2")),   //  2 - RED[2]
                redFromFrac.applyAsInt(param.get("redFracIn3")),   //  3 - RED[3]
                redFromFrac.applyAsInt(param.get("redFracIn4")),   //  4 - RED[4]
                blueFromFrac.applyAsInt(param.get("redFracIn0")),   //  5 - BLUE[0]
                blueFromFrac.applyAsInt(param.get("redFracIn1")),   //  6 - BLUE[1]
                blueFromFrac.applyAsInt(param.get("redFracIn2")),   //  7 - BLUE[2]
                blueFromFrac.applyAsInt(param.get("redFracIn3")),   //  8 - BLUE[3]
                blueFromFrac.applyAsInt(param.get("redFracIn4")),   //  9 - BLUE[4]
                16,  // 10 - UNC[0]
                16,  // 11 - UNC[1]
                16,  // 12 - UNC[2]
                16,  // 13 - UNC[3]
                16,  // 14 - UNC[4]
        });
    }
    private double robustnessInitialStateParametrized(PopulationModelDefinition currentModelDefinition, QuantitativeMonitor<PopulationState> currentMonitor, Map<String,Double> parameterization){
        return  robustness(currentModelDefinition.createModel(), getInitialRingState(parameterization),currentMonitor);
    }
    private double[] robustnessVector(PopulationModel currentModel, PopulationState currentInitialState, QuantitativeMonitor<PopulationState>[] currentMonitors){
        double[] robustnessVector = new double[currentMonitors.length];
        for (int i = 0; i < currentMonitors.length; i++) {
            robustnessVector[i] = robustness(currentModel,currentInitialState,currentMonitors[i]);
        }
        return robustnessVector;
    }
    private double[] robustnessVectorInitialStateParametrized(PopulationModelDefinition currentModelDefinition, QuantitativeMonitor<PopulationState>[] currentMonitors,Map<String,Double> parameterization){
        double[] robustnessVector = new double[currentMonitors.length];
        for (int i = 0; i < currentMonitors.length; i++) {
            robustnessVector[i] = robustnessInitialStateParametrized(currentModelDefinition,currentMonitors[i],parameterization);
        }
        return robustnessVector;
    }
    private HyperRectangle getSearchSpaceTspCompartment(){
        return new HyperRectangle(
                new ContinuousInterval("redFracIn0",0.1,1.0),
                new ContinuousInterval("redFracIn1",0.1,1.0),
                new ContinuousInterval("redFracIn2",0.1,1.0),
                new ContinuousInterval("redFracIn3",0.1,1.0),
                new ContinuousInterval("redFracIn4",0.1,1.0)
        );
    }
    private ToDoubleFunction<Map<String,Double>> getSurrogateFunction(ToDoubleFunction<Map<String,Double>> realFunction, HyperRectangle hr){
        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
                realFunction,
                new LatinHyperCubeSamplingTask(),
                hr,1000,0.95,
                new Properties());
        return randomForestModel.getSurrogateFunction(true);
    }

    private List<Predicate<Map<String, Double>>> getConstraintsList(){
        List<Predicate<Map<String, Double>>> constraints = new ArrayList<>();
        Predicate<Map<String,Double>> constraint1 = map ->
                map.get("redFracIn0") + map.get("redFracIn1")
                        + map.get("redFracIn2") + map.get("redFracIn3") + map.get("redFracIn4") >= 1.5
                &&
                map.get("redFracIn0") + map.get("redFracIn1")
                        + map.get("redFracIn2") + map.get("redFracIn3") + map.get("redFracIn4") <= 3.5;
        constraints.add(constraint1);
        return constraints;
    }

    private QuantitativeMonitor<PopulationState>[] getTSPMonitors(Map<String, ToDoubleFunction<PopulationState>> map) throws StlModelGenerationException {
        return new QuantitativeMonitor[]{
                getQuantitativeMonitor(getFormulaStability(),"formula_stability",new double[]{}, map),
                getQuantitativeMonitor(getFormulaCoherence(),"formula_coherence",new double[]{}, map),
                getQuantitativeMonitor(getFormulaRedPresence(),"formula_red",new double[]{}, map)
        };
    }

    private QuantitativeMonitor<PopulationState>[] getTSPBaseMonitors() throws StlModelGenerationException {
        return getTSPMonitors(getMeasureMapTSPBase());
    }
    private QuantitativeMonitor<PopulationState>[] getTSPRingMonitors() throws StlModelGenerationException {
        return getTSPMonitors(getMeasureMapTSPRing());
    }

    private void printResults(double[] baseRobustVector, Map<String,Double> solution,QuantitativeMonitor<PopulationState>[] mutatedMonitors,ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance  ){
        System.out.println();
        double[] variantRobustVector = robustnessVector(getPopulationModel(getTspRingString()),getInitialRingState(solution),mutatedMonitors);
        System.out.println("Robustness vector (base model): ");
        System.out.println("    > "+Arrays.toString(baseRobustVector));
        System.out.println("Robustness vector for optimal parameter (variant model): ");
        System.out.println("    > "+Arrays.toString(variantRobustVector));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Distances : ");
        System.out.println("    > Real          = "+this.euclideanDistance().apply(baseRobustVector,variantRobustVector));
        System.out.println("    > Surrogate     = "+surrogateRobustnessDistance.applyAsDouble(solution));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Solution founded:");
        printSolutionAsMap(solution);
        System.out.println("Sum of RED percentage : "+solution.values().stream().mapToDouble(Double::doubleValue).sum());
    }



    private void printResultsMaxBetter(double[] baseRobustVector, Map<String,Double> solution,QuantitativeMonitor<PopulationState>[] mutatedMonitors,ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance  ){
        System.out.println();
        double[] variantRobustVector = robustnessVector(getPopulationModel(getTspRingString()),getInitialRingState(solution),mutatedMonitors);
        System.out.println("Robustness vector (base model): ");
        System.out.println("    > "+Arrays.toString(baseRobustVector));
        System.out.println("Robustness vector for optimal parameter (variant model): ");
        System.out.println("    > "+Arrays.toString(variantRobustVector));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Distances : ");
        System.out.println("    > Real          = "+this.euclideanDistanceInMaximization().apply(variantRobustVector,baseRobustVector));
        System.out.println("    > Surrogate     = "+surrogateRobustnessDistance.applyAsDouble(solution));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Solution founded:");
        printSolutionAsMap(solution);
        System.out.println("Sum of RED percentage : "+solution.values().stream().mapToDouble(Double::doubleValue).sum());
    }

    private void printResultsMaxWorse(double[] baseRobustVector, Map<String,Double> solution,QuantitativeMonitor<PopulationState>[] mutatedMonitors,ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance  ){
        System.out.println();
        double[] variantRobustVector = robustnessVector(getPopulationModel(getTspRingString()),getInitialRingState(solution),mutatedMonitors);
        System.out.println("Robustness vector (base model): ");
        System.out.println("    > "+Arrays.toString(baseRobustVector));
        System.out.println("Robustness vector for optimal parameter (variant model): ");
        System.out.println("    > "+Arrays.toString(variantRobustVector));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Distances : ");
        System.out.println("    > Real          = "+this.euclideanDistanceInMaximization().apply(baseRobustVector,variantRobustVector));
        System.out.println("    > Surrogate     = "+surrogateRobustnessDistance.applyAsDouble(solution));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Solution founded:");
        printSolutionAsMap(solution);
        System.out.println("Sum of RED percentage : "+solution.values().stream().mapToDouble(Double::doubleValue).sum());
    }
    private Map<String,Double> minimizePreserving() throws StlModelGenerationException {

        QuantitativeMonitor<PopulationState>[] baseMonitors =  getTSPBaseMonitors();
        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  getTSPRingMonitors();

        double[] baseRobustVector = robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspRingString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistance().apply(baseRobustVector,mutatedRobustVector);

        };


        HyperRectangle hr = getSearchSpaceTspCompartment();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.minimize(surrogateRobustnessDistance,hr,getConstraintsList());
        printResults(baseRobustVector,solution,mutatedMonitors,surrogateRobustnessDistance);
        return solution;
    }

    private Map<String,Double> maximizeWorse() throws StlModelGenerationException{
        QuantitativeMonitor<PopulationState>[] baseMonitors =  getTSPBaseMonitors();
        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  getTSPRingMonitors();
        double[] baseRobustVector= robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspRingString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistanceInMaximization().apply(baseRobustVector,mutatedRobustVector);

        };

        HyperRectangle hr = getSearchSpaceTspCompartment();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.maximize(surrogateRobustnessDistance,hr,getConstraintsList());
        printResultsMaxWorse(baseRobustVector,solution,mutatedMonitors,surrogateRobustnessDistance);
        return solution;
    }

    private Map<String,Double> maximizeBetter() throws StlModelGenerationException{

        QuantitativeMonitor<PopulationState>[] baseMonitors =  getTSPBaseMonitors();
        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  getTSPRingMonitors();
        double[] baseRobustVector= robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspRingString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistanceInMaximization().apply(mutatedRobustVector,baseRobustVector);

        };
        HyperRectangle hr = getSearchSpaceTspCompartment();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.maximize(surrogateRobustnessDistance,hr,getConstraintsList());
        printResultsMaxBetter(baseRobustVector,solution,mutatedMonitors,surrogateRobustnessDistance);
        return solution;
    }
    @Disabled
    @Test
    public void testMinimizing() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();
         minimizePreserving();
    }
    @Disabled
    @Test
    public void testMaximizingBetter() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();
        maximizeBetter();
    }
    @Disabled
    @Test
    public void testMaximizingWorse() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();
        maximizeWorse();
    }

    @Disabled
    @Test
    public void testWhole() throws StlModelGenerationException {
        System.out.println("\n \n \n");
        System.out.println("MINIMIZING");
        System.out.println("\n");
        testMinimizing();
        System.out.println("\n \n \n");
        System.out.println("MAXIMIZING (worse)");
        System.out.println("\n");
        testMaximizingWorse();
        System.out.println("\n \n \n");
        System.out.println("MAXIMIZING (better)");
        System.out.println("\n");
        testMaximizingBetter();
    }


    @Disabled
    @Test
    public void testModel(){
        PopulationModel pm = getPopulationModel(getTspRingString());

        System.out.println(Arrays.toString(pm.measures()));

        Map<String,Double> parameters = new HashMap<>();

        parameters.put("redFracIn0",0.5);
        parameters.put("redFracIn1",0.5);
        parameters.put("redFracIn2",0.5);
        parameters.put("redFracIn3",0.5);
        parameters.put("redFracIn4",0.5);
        this.se  = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        Trajectory<PopulationState> t = sampleATrajectory(pm,getInitialRingState(parameters),20);
        System.out.println(t);
    }
    @Disabled
    @Test
    public void testPrintResult(){
        Map<String,Double> parameters = new HashMap<>();

        parameters.put("redFracIn0",0.264);
        parameters.put("redFracIn1",0.011);
        parameters.put("redFracIn2",0.32112);
        parameters.put("redFracIn3",0.4888);
        parameters.put("redFracIn4",0.8923);

        printSolutionAsMap(parameters);
    }


    public  void printSolutionAsMap(Map<String, Double> parameters) {
        int n = 20;
        int k = 5;
        ToIntFunction<Double> redFromFrac = fraction -> Math.toIntExact(Math.round(fraction * n / k));
        ToIntFunction<Double> blueFromFrac = fraction -> Math.toIntExact(Math.round((1 - fraction) * n / k));

        // Use TreeMap to automatically sort the keys
        TreeMap<String, Double> sortedParameters = new TreeMap<>(parameters);

        System.out.println("percentage");
        int totalRed = 0;
        int totalBlue = 0;

        for (Map.Entry<String, Double> entry : sortedParameters.entrySet()) {
            String key = entry.getKey();
            double fraction = entry.getValue();
            int red = redFromFrac.applyAsInt(fraction);
            int blue = blueFromFrac.applyAsInt(fraction);
            totalRed += red;
            totalBlue += blue;

            System.out.printf("%%RED_%s = %.2f             %%BLUE_%s = %.2f\n", key.substring(key.length() - 1), fraction, key.substring(key.length() - 1), 1 - fraction);
        }

        System.out.println("__________________________________________");
        System.out.println("quantity");

        for (Map.Entry<String, Double> entry : sortedParameters.entrySet()) {
            String key = entry.getKey();
            double fraction = entry.getValue();
            int red = redFromFrac.applyAsInt(fraction);
            int blue = blueFromFrac.applyAsInt(fraction);

            System.out.printf("#RED_%s = %d             #BLUE_%s = %d\n", key.substring(key.length() - 1), red, key.substring(key.length() - 1), blue);
        }

        System.out.println("--------------------------------------------------");
        System.out.printf("#RED = %d \n#BLUE = %d\n", totalRed, totalBlue);
    }
}




