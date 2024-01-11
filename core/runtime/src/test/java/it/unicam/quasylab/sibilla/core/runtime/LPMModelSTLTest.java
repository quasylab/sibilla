package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.FullFactorialSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.DataSet;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.RandomForest;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModel;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.Signal;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import it.unicam.quasylab.sibilla.langs.stl.StlLoader;
import it.unicam.quasylab.sibilla.langs.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.langs.stl.StlMonitorFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPMModelSTLTest {


    @SuppressWarnings("All")
    private void storeCSV(DataSet dataSet, String outputFolder, String fileName) throws IOException {
        CSVWriter writer = new CSVWriter(outputFolder, "", "");
        writer.write(fileName,dataSet);
    }


    /**
     * The LPMModel class represents a simulation model for a specific population system.
     * It provides methods for setting parameters, generating trajectories, and computing various
     * metrics and measures based on the trajectories.
     */
    private static class LPMModel{

        final private SimulationEnvironment se;
        final private RandomGenerator rg;
        final private PopulationModelDefinition pmd;
        PopulationState initialState;
        private final QuantitativeMonitor<PopulationState> quantitativeMonitor;
        private final QualitativeMonitor<PopulationState> qualitativeMonitor;
        private PopulationModel pm;
        private int numberOfTrajectories;

        public LPMModel(String modelDefinition,
                        String formulaDefinition,
                        PopulationState initialState,
                        Map<String, ToDoubleFunction<PopulationState>> measureMapFunction,
                        String formulaName,
                        double[] formulaParameter,
                        int numberOfTrajectories) throws ModelGenerationException, StlModelGenerationException {
            this.numberOfTrajectories = numberOfTrajectories;
            this.initialState = initialState;
            this.se = new SimulationEnvironment();
            this.rg = new DefaultRandomGenerator();
            PopulationModelGenerator pmg = new PopulationModelGenerator(modelDefinition);
            this.pmd = pmg.getPopulationModelDefinition();
            this.pm = pmd.createModel();
            StlLoader loader = new StlLoader(formulaDefinition);
            StlMonitorFactory<PopulationState> monitorFactory = loader.getModelFactory(measureMapFunction);
            this.quantitativeMonitor = monitorFactory.getQuantitativeMonitor(formulaName, formulaParameter);
            this.qualitativeMonitor = monitorFactory.getQualitativeMonitor(formulaName, formulaParameter);
        }
        @SuppressWarnings("unused")
        public void setNumberOfTrajectories(int numberOfTrajectories) {
            this.numberOfTrajectories = numberOfTrajectories;
        }
        @SuppressWarnings("unused")
        public void setSeed(long seed){
            this.rg.setSeed(seed);
        }

        public LPMModel(String modelDefinition,
                        String formulaDefinition,
                        PopulationState initialState,
                        Map<String, ToDoubleFunction<PopulationState>> measureMapFunction,
                        String formulaName) throws StlModelGenerationException, ModelGenerationException {
            this(modelDefinition, formulaDefinition,initialState, measureMapFunction, formulaName, new double[]{}, 100);
        }

        private void setParameters(Map<String,Double> parameters){
            for (String key : parameters.keySet()){
                this.pmd.setParameter(key,new SibillaDouble(parameters.get(key)));
            }
            this.pm = this.pmd.createModel();
        }

        private double getTimiHorizon(){
            return this.quantitativeMonitor.getTimeHorizon();
        }

        private Trajectory<PopulationState> sampleATrajectory(){
            Trajectory<PopulationState> trajectory= this.se.sampleTrajectory(
                    this.rg,
                    this.pm,
                    this.initialState,
                    this.getTimiHorizon());
            trajectory.setEnd(getTimiHorizon());
            return trajectory;
        }

        public double averageRobustness(){
            double r = 0;
            for (int i = 0; i < this.numberOfTrajectories; i++) {
                Trajectory<PopulationState> trj  = sampleATrajectory();
                Signal monitoredSignal = quantitativeMonitor.monitor(trj);
                double currentRobustness = monitoredSignal.valueAt(0);
                r += currentRobustness;
            }
            return  r/this.numberOfTrajectories;
        }

        @SuppressWarnings("unused")
        public ToDoubleFunction<Map<String,Double>> getAverageRobustnessFunction(){
            return m -> {
                this.setParameters(m);
                return this.averageRobustness();
            };
        }

        public double probReachFormulaAtTime0(){
            double[] probabilities = QualitativeMonitor
                    .computeProbability(
                            this.qualitativeMonitor,
                            this::sampleATrajectory,
                            this.numberOfTrajectories,
                            new double[]{0.0}
                    );
            return probabilities[0];
        }


        public ToDoubleFunction<Map<String,Double>> getProbReachFormulaAtTime0Function(){
            return m -> {
                this.setParameters(m);
                return this.probReachFormulaAtTime0();
            };
        }




    }



    /**
     * This test case verify the effectiveness of optimization over a SIR epidemiological model.
     * The case model and condition referred in this test, can be found
     * at <a href="https://arxiv.org/pdf/1402.1450.pdf">the case</a>.
     * The parameter ranges considered are:
     * k_i (rate of infection) - between 0.005 and 0.3
     * k_r (rate of recovery) - between 0.005 and 0.2
     * The test aims to find the maximum or optimal values for these parameters. In this case,
     * the optimal values found are:
     * max -> 0.35
     * k_i -> 0.25
     * k_r -> 0.05
     * The test employs the Sibilla probabilistic model checker, which generates a surrogate model using random forest.
     * It then uses the Particle Swarm Optimization (PSO) technique to find the global maximum
     * within the parameter space (HyperRectangle).
     * The test validates the resulting maximum values against the expected optimal parameters,
     * asserting that the difference is within a specified tolerance.
     *
     * @throws StlModelGenerationException if there are issues in generating the STL model
     * @throws ModelGenerationException if there are issues in creating the SIR model or the surrogate model
     */
    @Disabled
    @Test
    public void testSirCase() throws StlModelGenerationException, ModelGenerationException{
        String formula =  """
                measure #I
                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
                """;

        String sirModel = """
                param k_i = 0.05;
                param k_r = 0.05;
                    
                species S;
                species I;
                species R;
                                            
                rule infection {
                    S|I -[ #S * %I * k_i ]-> I|I
                }
                                            
                rule recovered {
                    I -[ #I * k_r ]-> R
                }
                """;

        PopulationState initialState = new PopulationState(new int[]{10, 90, 0});

        Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
        measuresMapping.put("#S", s -> s.getOccupancy(0));
        measuresMapping.put("#I", s -> s.getOccupancy(1));
        measuresMapping.put("#R", s -> s.getOccupancy(2));

        LPMModel sirModelTester = new LPMModel(sirModel,
                formula, initialState, measuresMapping, "formula_id");
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_i",0.005,0.3),
                new ContinuousInterval("k_r",0.005,0.2));


        ToDoubleFunction<Map<String,Double>> function = sirModelTester.getProbReachFormulaAtTime0Function();

        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
                function,
                new LatinHyperCubeSamplingTask(),
                hr,1000,0.95,
                new Properties());

        ToDoubleFunction<Map<String,Double>> surrogateFunction = randomForestModel.getSurrogateFunction(true);

        Map<String,Double> maximizingValues = new PSOTask().maximize(surrogateFunction,hr);

        assertEquals(maximizingValues.get("k_i"),0.25,0.1);
        assertEquals(maximizingValues.get("k_r"),0.05,0.1);
    }





    @Disabled
    @Test
    public void testRumorSpreadingCase() throws StlModelGenerationException, ModelGenerationException, IOException {

        String formula= """
                measure #S
                measure #I
                formula formula_id [] : ( \\G[3,5]( [ #I > 0 ] ) )  && ( \\E[0,1] ( \\G[0,0.02] ( [#S > 50 ] ) ) )  endformula
                """;


        String rumorSpreadModel = """
                param k_s = 0.05;
                param k_r = 0.05;
                
                species S;
                species I;
                species B;
                
                
                rule spreading {
                    S|I -[ #S * %I * k_s ]-> S|S
                }
                
                rule stop_spreading_1 {
                    S|B -[ #S * (#S - 1) * k_r ]-> B|B
                }
                
                rule stop_spreading_2 {
                    S|B -[ #B * %S * k_r]-> B|B
                }
                """;


        PopulationState initialState = new PopulationState(new int[]{10, 90, 0});

        Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
        measuresMapping.put("#S", s -> s.getOccupancy(0));
        measuresMapping.put("#I", s -> s.getOccupancy(1));
        measuresMapping.put("#B", s -> s.getOccupancy(2));



        LPMModel rsModelTester = new LPMModel(rumorSpreadModel,
                formula, initialState, measuresMapping, "formula_id");

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_s",0.0001,2.0),
                new ContinuousInterval("k_r",0.0001,0.5));

        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),20, rsModelTester.getProbReachFormulaAtTime0Function());

        storeCSV(dataSetProbReach,"path", "testRS_prob");
    }

}
