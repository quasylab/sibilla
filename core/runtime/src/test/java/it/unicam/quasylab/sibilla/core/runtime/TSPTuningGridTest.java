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
@SuppressWarnings("all")

public class TSPTuningGridTest {

    private int numberOfTrajectories;
    private SimulationEnvironment se;
    private RandomGenerator rg;
    /**
     * STABILITY : majority or consensus are constants over time
     * majority  : %RED >= 1.0 || %BLUE >= 1.0
     */
    private String getFormulaStability(){
        return """
                measure %RED
                measure %BLUE
                formula formula_stability [] : \\E[0,100][ %RED >= 1.0 ]  || \\E[0,100][ %BLUE >= 1.0 ]   endformula
                """;
    }
    /**
     * COHERENCE : if one faction is bigger than the other, it wins
     * bigger red  : ((%RED>%BLUE)->(\E[0,100][%RED>=1.0]))
     * AND
     * bigger blue : ((%BLUE>%RED)->(\E[0,100][%BLUE>=1.0]))
     */
//    private String getFormulaCoherence(){
//        return """
//                measure %RED
//                measure %BLUE
//                formula formula_coherence [] : ([%RED>%BLUE]->(\\E[0,100][%RED>=1.0])) && ([%BLUE>%RED]->(\\E[0,100][%BLUE>=1.0])) endformula
//                """;
//    }

    private String getFormulaCoherence(){
        return """
                measure %RED
                measure %BLUE
                formula formula_coherence [] : ([%RED>%BLUE]->(\\E[0,100][%RED>=1.0])) endformula
                """;
    }

    private String getFormulaRedCoherence(){
        return """
                measure %RED
                measure %BLUE
                formula formula_coherence [] : ([%RED>%BLUE]->(\\E[0,100][%RED>=1.0])) endformula
                """;
    }

    private String getIfRedIsMajorityImpliesRedWin(){
        return """
                measure %RED
                formula formula_coherence [] : ([%RED>%BLUE]->(\\E[0,100][%RED>=1.0]))  endformula
                """;
    }

    private String getIfBlueIsMajorityImpliesBlueWin(){
        return """
                measure %BLUE
                formula formula_coherence [] :  ([%BLUE>%RED]->(\\E[0,100][%BLUE>=1.0])) endformula
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
    private String getTspCompartmentString(){
        return """
                  const N = 3;       /* Width of the Grid */
                  const M = 3;       /* Length of the Grid */
                
                  species RED of [0,N]*[0,M];
                  species BLUE of [0,N]*[0,M];
                  species UNC of [0,N]*[0,M];
               
                  param on_site_persuasion = 1.0;
                  param adjacent_persuasion = 1.0 ;
                
                  param red_00 = 1.0;
                  param red_01 = 1.0;
                  param red_02 = 1.0;
                  param red_10 = 1.0;
                  param red_11 = 1.0;
                  param red_12 = 1.0;
                  param red_20 = 1.0;
                  param red_21 = 1.0;
                  param red_22 = 1.0;
                  param blue_00 = 1.0;
                  param blue_01 = 1.0;
                  param blue_02 = 1.0;
                  param blue_10 = 1.0;
                  param blue_11 = 1.0;
                  param blue_12 = 1.0;
                  param blue_20 = 1.0;
                  param blue_21 = 1.0;
                  param blue_22 = 1.0;
                
                
                
                  rule on_site_UNC_to_BLUE for i in [0,N] and j in [0,M] {
                    UNC[i,j]|BLUE[i,j] -[ #UNC[i,j] * on_site_persuasion * %BLUE[i,j] ]-> BLUE[i,j]|BLUE[i,j]
                  }
                
                  rule on_site_UNC_to_RED for i in [0,N] and j in [0,M] {
                    UNC[i,j]|RED[i,j] -[ #UNC[i,j] * on_site_persuasion * %RED[i,j] ]-> RED[i,j]|RED[i,j]
                  }
                
                
                
                  rule up_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when j<M-1 {
                    UNC[i,j+1]|BLUE[i,j] -[ #UNC[i,j+1] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i,j+1]|BLUE[i,j]
                  }
                
                  rule down_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when j>0 {
                    UNC[i,j-1]|BLUE[i,j] -[ #UNC[i,j-1] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i,j-1]|BLUE[i,j]
                  }
                
                  rule right_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when i<N-1 {
                    UNC[i+1,j]|BLUE[i,j] -[ #UNC[i+1,j] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i+1,j]|BLUE[i,j]
                  }
                
                  rule left_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when i>0 {
                    UNC[i-1,j]|BLUE[i,j] -[ #UNC[i-1,j] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i-1,j]|BLUE[i,j]
                  }
                
                
                
                  rule up_site_UNC_to_RED for i in [0,N] and j in [0,M] when j<M-1 {
                    UNC[i,j+1]|RED[i,j] -[ #UNC[i,j+1] * adjacent_persuasion * %RED[i,j] ]-> RED[i,j+1]|RED[i,j]
                  }
                
                  rule down_site_UNC_to_RED for i in [0,N] and j in [0,M] when j>0 {
                    UNC[i,j-1]|RED[i,j] -[ #UNC[i,j-1] * adjacent_persuasion * %RED[i,j] ]-> RED[i,j-1]|RED[i,j]
                  }
                
                  rule right_site_UNC_to_RED for i in [0,N] and j in [0,M] when i<N-1 {
                    UNC[i+1,j]|RED[i,j] -[ #UNC[i+1,j] * adjacent_persuasion * %RED[i,j] ]-> RED[i+1,j]|RED[i,j]
                  }
                
                  rule left_site_UNC_to_RED for i in [0,N] and j in [0,M] when i>0 {
                    UNC[i-1,j]|RED[i,j] -[ #UNC[i-1,j] * adjacent_persuasion * %RED[i,j] ]-> RED[i-1,j]|RED[i,j]
                  }
                
                
                
                  rule on_site_BLUE_to_UNC for i in [0,N] and j in [0,M] {
                    BLUE[i,j]|RED[i,j] -[ #BLUE[i,j] * on_site_persuasion * %RED[i,j] ]-> UNC[i,j]|RED[i,j]
                  }
                
                  rule on_site_RED_to_UNC for i in [0,N] and j in [0,M] {
                    RED[i,j]|BLUE[i,j] -[ #RED[i,j] * on_site_persuasion * %BLUE[i,j] ]-> UNC[i,j]|BLUE[i,j]
                  }
                
                
                  rule up_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when j<M-1{
                    BLUE[i,j]|RED[i,j+1] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i,j+1] ]-> UNC[i,j]|RED[i,j+1]
                  }
                
                  rule down_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when j>0{
                    BLUE[i,j]|RED[i,j-1] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i,j-1] ]-> UNC[i,j]|RED[i,j-1]
                  }
                
                  rule right_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when i<N-1{
                    BLUE[i,j]|RED[i+1,j] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i+1,j] ]-> UNC[i,j]|RED[i+1,j]
                  }
                
                  rule left_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when i>0{
                    BLUE[i,j]|RED[i-1,j] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i-1,j] ]-> UNC[i,j]|RED[i-1,j]
                  }
                
                
                  rule up_site_RED_to_UNC for i in [0,N] and j in [0,M] when j<M-1{
                    RED[i,j]|BLUE[i,j+1] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i,j+1] ]-> UNC[i,j]|BLUE[i,j+1]
                  }
                
                  rule down_site_RED_to_UNC for i in [0,N] and j in [0,M] when j>0{
                    RED[i,j]|BLUE[i,j-1] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i,j-1] ]-> UNC[i,j]|BLUE[i,j-1]
                  }
                
                  rule right_site_RED_to_UNC for i in [0,N] and j in [0,M] when i<N-1{
                    RED[i,j]|BLUE[i+1,j] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i+1,j] ]-> UNC[i,j]|BLUE[i+1,j]
                  }
                
                  rule left_site_RED_to_UNC for i in [0,N] and j in [0,M] when i>0{
                    RED[i,j]|BLUE[i-1,j] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i-1,j] ]-> UNC[i,j]|BLUE[i-1,j]
                  }
                
                
                  measure redPercentage = %RED[0,0] + %RED[0,1] + %RED[0,2] + %RED[1,0] + %RED[1,1] + %RED[1,2] + %RED[2,0] + %RED[2,1] + %RED[2,2];
                  measure bluePercentage = %BLUE[0,0] + %BLUE[0,1] + %BLUE[0,2] + %BLUE[1,0] + %BLUE[1,1] + %BLUE[1,2] + %BLUE[2,0] + %BLUE[2,1] + %BLUE[2,2];
                  measure uncPercentage = %UNC[0,0] + %UNC[0,1] + %UNC[0,2] + %UNC[1,0] + %UNC[1,1] + %UNC[1,2] + %UNC[2,0] + %UNC[2,1] + %UNC[2,2];
                
                  measure redQuantity = #RED[0,0] + #RED[0,1] + #RED[0,2] + #RED[1,0] + #RED[1,1] + #RED[1,2] + #RED[2,0] + #RED[2,1] + #RED[2,2];
                  measure blueQuantity  = #BLUE[0,0] + #BLUE[0,1] + #BLUE[0,2] + #BLUE[1,0] + #BLUE[1,1] + #BLUE[1,2] + #BLUE[2,0] + #BLUE[2,1] + #BLUE[2,2];
                  measure uncQuantity  = #UNC[0,0] + #UNC[0,1] + #UNC[0,2] + #UNC[1,0] + #UNC[1,1] + #UNC[1,2] + #UNC[2,0] + #UNC[2,1] + #UNC[2,2];
                
                  system initial = RED[0,0]<1*red_00>|RED[0,1]<1*red_01>|RED[0,2]<1*red_02>|RED[1,0]<1*red_10>|RED[1,1]<1*red_11>|RED[1,2]<1*red_12>|RED[2,0]<1*red_20>|RED[2,1]<1*red_21>|RED[2,2]<1*red_22>|BLUE[0,0]<1*blue_00>|BLUE[0,1]<1*blue_01>|BLUE[0,2]<1*blue_02>|BLUE[1,0]<1*blue_10>|BLUE[1,1]<1*blue_11>|BLUE[1,2]<1*blue_12>|BLUE[2,0]<1*blue_20>|BLUE[2,1]<1*blue_21>|BLUE[2,2]<1*blue_22>;
                
                  system initial_cross = RED[0,1]<1*red_01>|RED[1,0]<1*red_10>|RED[1,1]<1*red_11>|RED[1,2]<1*red_12>|RED[2,1]<1*red_21>|BLUE[0,1]<1*blue_01>|BLUE[1,0]<1*blue_10>|BLUE[1,1]<1*blue_11>|BLUE[1,2]<1*blue_12>|BLUE[2,1]<1*blue_21>;
                
                """;
    }
    private Map<String, ToDoubleFunction<PopulationState>> getMeasureMapTSPBase(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSP = new HashMap<>();
        measuresMappingTSP.put("%RED", s -> s.getFraction(0));
        measuresMappingTSP.put("%BLUE", s -> s.getFraction(1));
        return measuresMappingTSP;
    }
    private Map<String, ToDoubleFunction<PopulationState>> getMeasureMapTSPCompartment(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSP = new HashMap<>();
        measuresMappingTSP.put("%RED", s -> s.getFraction(3,1,4,7,5));
        measuresMappingTSP.put("%BLUE", s -> s.getFraction(12,10,13,16,14));
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
//    private double robustnessParametrized(PopulationModelDefinition currentModelDefinition, PopulationState currentInitialState, QuantitativeMonitor<PopulationState> currentMonitor, Map<String,Double> parameterization){
//        for (String key : parameterization.keySet()){
//            currentModelDefinition.setParameter(key,new SibillaDouble(parameterization.get(key)));
//        }
//        return  robustness(currentModelDefinition.createModel(),currentInitialState,currentMonitor);
//    }
    private PopulationState getBaseInitialState(){
        return new PopulationState(new int[]{
                15,  // RED
                5, // BLUE
                80  // UNC
        });
    }

    private PopulationState getBaseInitialState(int red, int blue, int unc){
        return new PopulationState(new int[]{
                red,  // RED
                blue, // BLUE
                unc  // UNC
        });
    }
    private PopulationState getInitialCrossState(Map<String,Double> param){
        return new PopulationState(new int[]{
                0,   // RED[0,0]
                (int) Math.round(param.get("RED10")),   // RED[1,0]
                0,   // RED[2,0]
                (int) Math.round(param.get("RED01")),   // RED[0,1]
                (int) Math.round(param.get("RED11")),   // RED[1,1]
                (int) Math.round(param.get("RED21")),   // RED[2,1]
                0,   // RED[0,2]
                (int) Math.round(param.get("RED12")),   // RED[1,2]
                0,   // RED[2,2]
                0,   // BLUE[0,0]
                (int) Math.round(param.get("BLUE10")),   // BLUE[1,0]
                0,   // BLUE[2,0]
                (int) Math.round(param.get("BLUE01")),   // BLUE[0,1]
                (int) Math.round(param.get("BLUE11")),   // BLUE[1,1]
                (int) Math.round(param.get("BLUE21")),   // BLUE[2,1]
                0,   // BLUE[0,2]
                (int) Math.round(param.get("BLUE12")),   // BLUE[1,2]
                0,   // BLUE[2,2]
                0,   // UNC[0,0]
                16,  // UNC[1,0]
                0,   // UNC[2,0]
                16,  // UNC[0,1]
                16,  // UNC[1,1]
                16,  // UNC[2,1]
                0,   // UNC[0,2]
                16,  // UNC[1,2]
                0    // UNC[2,2]
        });
    }
    private double robustnessInitialStateParametrized(PopulationModelDefinition currentModelDefinition, QuantitativeMonitor<PopulationState> currentMonitor, Map<String,Double> parameterization){
        return  robustness(currentModelDefinition.createModel(),getInitialCrossState(parameterization),currentMonitor);
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
                new ContinuousInterval("RED10",0.1,5.0),
                new ContinuousInterval("RED01",0.1,5.0),
                new ContinuousInterval("RED11",0.1,5.0),
                new ContinuousInterval("RED21",0.1,5.0),
                new ContinuousInterval("RED12",0.1,5.0),
                new ContinuousInterval("BLUE10",0.1,5.0),
                new ContinuousInterval("BLUE01",0.1,5.0),
                new ContinuousInterval("BLUE11",0.1,5.0),
                new ContinuousInterval("BLUE21",0.1,5.0),
                new ContinuousInterval("BLUE12",0.1,5.0)
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
        Predicate<Map<String, Double>> sumConstraintGreaterThan = param -> param.get("RED10") + param.get("RED01")
                + param.get("RED11") + param.get("RED21") + param.get("RED12") + param.get("BLUE10")
                + param.get("BLUE01") + param.get("BLUE11") + param.get("BLUE21") + param.get("BLUE12") >= 18.0;

        Predicate<Map<String, Double>> sumConstraintSmallerThan = param -> param.get("RED10") + param.get("RED01")
                + param.get("RED11") + param.get("RED21") + param.get("RED12") + param.get("BLUE10")
                + param.get("BLUE01") + param.get("BLUE11") + param.get("BLUE21") + param.get("BLUE12") <= 20.0;

        constraints.add(sumConstraintGreaterThan);
        constraints.add(sumConstraintSmallerThan);
        return constraints;
    }

    private Map<String,Double> minimizeThroughSurrogate(ToDoubleFunction<Map<String,Double>> function,HyperRectangle hr, List<Predicate<Map<String, Double>>> constraints){
        return new PSOTask().minimize(getSurrogateFunction(function,hr),hr,constraints);
    }
    private Map<String,Double> minimizePreserving() throws StlModelGenerationException {

        QuantitativeMonitor<PopulationState>[] baseMonitors =  new QuantitativeMonitor[]{
                getQuantitativeMonitor(getFormulaStability(),"formula_stability",new double[]{},getMeasureMapTSPBase()),
                getQuantitativeMonitor(getFormulaCoherence(),"formula_coherence",new double[]{},getMeasureMapTSPBase())
        };

        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  new QuantitativeMonitor[]{
                getQuantitativeMonitor(getFormulaStability(),"formula_stability",new double[]{},getMeasureMapTSPCompartment()),
                getQuantitativeMonitor(getFormulaCoherence(),"formula_coherence",new double[]{},getMeasureMapTSPCompartment())
        };

        double[] baseRobustVector= robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        System.out.println(Arrays.toString(baseRobustVector));

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspCompartmentString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistance().apply(baseRobustVector,mutatedRobustVector);

        };

        List<Predicate<Map<String, Double>>> constraintList = getConstraintsList();
        HyperRectangle hr = getSearchSpaceTspCompartment();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.minimize(surrogateRobustnessDistance,hr,constraintList);
        System.out.println();
        System.out.println("distance : " + surrogateRobustnessDistance.applyAsDouble(solution));
        return solution;
    }

    private Map<String,Double> maximizeWorse() throws StlModelGenerationException{
        QuantitativeMonitor<PopulationState>[] baseMonitors =  new QuantitativeMonitor[]{
                getQuantitativeMonitor(getFormulaStability(),"formula_stability",new double[]{},getMeasureMapTSPBase()),
                getQuantitativeMonitor(getFormulaCoherence(),"formula_coherence",new double[]{},getMeasureMapTSPBase())
        };

        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  new QuantitativeMonitor[]{
                getQuantitativeMonitor(getFormulaStability(),"formula_stability",new double[]{},getMeasureMapTSPCompartment()),
                getQuantitativeMonitor(getFormulaCoherence(),"formula_coherence",new double[]{},getMeasureMapTSPCompartment())
        };
        double[] baseRobustVector= robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        System.out.println(Arrays.toString(baseRobustVector));

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspCompartmentString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistance().apply(baseRobustVector,mutatedRobustVector);

        };

        List<Predicate<Map<String, Double>>> constraintList = getConstraintsList();
        HyperRectangle hr = getSearchSpaceTspCompartment();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.maximize(surrogateRobustnessDistance,hr,constraintList);
        System.out.println();
        System.out.println("distance : " + surrogateRobustnessDistance.applyAsDouble(solution));
        return solution;
    }

    private Map<String,Double> maximizeBetter() throws StlModelGenerationException{

        QuantitativeMonitor<PopulationState>[] baseMonitors =  new QuantitativeMonitor[]{
                getQuantitativeMonitor(getFormulaStability(),"formula_stability",new double[]{},getMeasureMapTSPBase()),
                getQuantitativeMonitor(getFormulaCoherence(),"formula_coherence",new double[]{},getMeasureMapTSPBase())
        };

        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  new QuantitativeMonitor[]{
                getQuantitativeMonitor(getFormulaStability(),"formula_stability",new double[]{},getMeasureMapTSPCompartment()),
                getQuantitativeMonitor(getFormulaCoherence(),"formula_coherence",new double[]{},getMeasureMapTSPCompartment())
        };
        double[] baseRobustVector= robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        System.out.println(Arrays.toString(baseRobustVector));

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspCompartmentString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistanceInMaximization().apply(mutatedRobustVector,baseRobustVector);

        };

        List<Predicate<Map<String, Double>>> constraintList = getConstraintsList();
        HyperRectangle hr = getSearchSpaceTspCompartment();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.maximize(surrogateRobustnessDistance,hr,constraintList);
        //Map<String,Double> solution = optTask.maximize(surrogateRobustnessDistance,hr);
        System.out.println();
        System.out.println("distance : " + surrogateRobustnessDistance.applyAsDouble(solution));
        return solution;
    }
    @Disabled
    @Test
    public void testMinimizing() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();

        Map<String,Double> result = minimizePreserving();
        printResult(result);

    }
    @Disabled
    @Test
    public void testMaximizingBetter() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();

        Map<String,Double> result = maximizeBetter();
        printResult(result);
    }
    @Disabled
    @Test
    public void testMaximizingBWorse() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();

        Map<String,Double> result = maximizeWorse();
        printResult(result);
    }


    private void printResultRaw(Map<String, Double> result) {
        System.out.println();
        result.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Sort the entries by key
                .forEach(entry ->
                        System.out.println(entry.getKey() + ": " + entry.getValue())
                );
        System.out.println("Sum = " + result.values().stream().reduce(0.0, Double::sum));
    }





    private void printResult(Map<String, Double> result) {
        // Split the map into two: one for RED and one for BLUE
        Map<String, Double> redResults = new TreeMap<>();
        Map<String, Double> blueResults = new TreeMap<>();

        printResultRaw(result);
        System.out.println();

        result.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    if (entry.getKey().startsWith("RED")) {
                        redResults.put(entry.getKey(), entry.getValue());
                    } else if (entry.getKey().startsWith("BLUE")) {
                        blueResults.put(entry.getKey(), entry.getValue());
                    }
                });

        int redSum = 0;
        int blueSum = 0;

        // Iterate through RED results and find matching BLUE results to print in pairs
        for (String key : redResults.keySet()) {
            Double redValue = redResults.get(key);
            String matchingKey = key.replace("RED", "BLUE");
            Double blueValue = blueResults.get(matchingKey);

            int roundedRedValue = Math.round(redValue.floatValue());
            int roundedBlueValue = blueValue != null ? Math.round(blueValue.floatValue()) : 0; // Default to 0 if not found

            System.out.println(key + " = " + roundedRedValue);
            System.out.println(matchingKey + " = " + roundedBlueValue);

            redSum += roundedRedValue;
            blueSum += roundedBlueValue;
        }

        System.out.println("Sum of rounded RED = " + redSum);
        System.out.println("Sum of rounded BLUE = " + blueSum);
    }


    @Disabled
    @Test
    public void testPrintMap(){
        Map<String,Double> map = new HashMap<>();
        map.put("RED01",1.5318547470358417);
        map.put("RED12",2.8633265079859);
        map.put("RED11",3.047896160007452);
        map.put("RED10",0.6545166999095794);
        map.put("RED21",4.532005575299949);
        map.put("BLUE11",4.98043085407031);
        map.put("BLUE01",0.10556011608984918);
        map.put("BLUE12",0.4070430412623753);
        map.put("BLUE10",0.9962569894191036);
        map.put("BLUE21",0.8782414231035662);
        printResult(map);
    }
    @Disabled
    @Test
    public void testEu(){
        double[] vec1 = new double[]{1.0,1.0,1.0};
        double[] vec2 = new double[]{5.0,5.0,5.0};


        System.out.println("normal");
        System.out.println(euclideanDistance().apply(vec1,vec2));
        System.out.println(euclideanDistance().apply(vec2,vec1));

        System.out.println("modified");
        System.out.println(euclideanDistanceInMaximization().apply(vec1,vec2));
        System.out.println(euclideanDistanceInMaximization().apply(vec2,vec1));
    }


    @Disabled
    @Test
    public void testNumberOfAgent(){
        int n = 20;
        int k = 5;
        ToIntFunction<Double> red = fraction -> Math.toIntExact(Math.round(fraction * n / k));
        ToIntFunction<Double> blue = fraction -> Math.toIntExact(Math.round((1-fraction) * n / k));

        System.out.println("red : "+red.applyAsInt(0.3)+ " blue : "+ blue.applyAsInt(0.3));
        System.out.println("red : "+red.applyAsInt(0.2)+ " blue : "+ blue.applyAsInt(0.2));
        System.out.println("red : "+red.applyAsInt(0.8)+ " blue : "+ blue.applyAsInt(0.8));
        System.out.println("red : "+red.applyAsInt(0.9)+ " blue : "+ blue.applyAsInt(0.9));
        System.out.println("red : "+red.applyAsInt(0.1)+ " blue : "+ blue.applyAsInt(0.1));

    }

//    @Test
//    public void testModule(){
//        ToIntBiFunction<Integer,Integer> fun1 = (i,k) -> (i-1+k)%k;
//        ToIntBiFunction<Integer,Integer> fun2 = (i,k) -> (i+1)%k;
//
//
//        System.out.println("prev of 0 : " +fun1.applyAsInt(0,5));
//        System.out.println("prev of 1 : " +fun1.applyAsInt(1,5));
//        System.out.println("prev of 2 : " +fun1.applyAsInt(2,5));
//        System.out.println("prev of 3 : " +fun1.applyAsInt(3,5));
//        System.out.println("prev of 4 : " +fun1.applyAsInt(4,5));
//
//        System.out.println("next of 0 : " +fun2.applyAsInt(0,5));
//        System.out.println("next of 1 : " +fun2.applyAsInt(1,5));
//        System.out.println("next of 2 : " +fun2.applyAsInt(2,5));
//        System.out.println("next of 3 : " +fun2.applyAsInt(3,5));
//        System.out.println("next of 4 : " +fun2.applyAsInt(4,5));
//
//    }

}
