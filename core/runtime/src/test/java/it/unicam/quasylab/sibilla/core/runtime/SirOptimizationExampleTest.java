package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.optimization.sampling.FullFactorialSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.DataSet;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class SirOptimizationExampleTest {

    private static class SirExample{

        final private SimulationEnvironment se;
        final private RandomGenerator rg;
        final private PopulationModelDefinition pmd;
        private PopulationModel pm;
        final private QuantitativeMonitor<PopulationState> formula;

        private int numberOfTrajectories;

        final String FORMULA_SIR =
                """
                measure #I; 
                formula formula_id [] : ( \\E[100,120][ #I < 1] )&& (\\G[0,100][ #I > 0 ]) ;
                """;
        final String MODEL___SIR =
                """
                param k_i = 0.05;
                param k_r = 0.05;
                
                const startS = 95;
                const startI = 5;
                const startR = 0;
                                            
                species S;
                species I;
                species R;
                                            
                rule infection {
                    S|I -[ #S * %I * k_i ]-> I|I
                }
                                            
                rule recovered {
                    I -[ #I * k_r ]-> R
                }
                                           
                system init = S<startS>|I<startI>|R<startR>;
                """;
        public SirExample() throws ModelGenerationException, StlModelGenerationException {
            this.se = new SimulationEnvironment();
            this.rg = new DefaultRandomGenerator();
            PopulationModelGenerator pmg = new PopulationModelGenerator(this.MODEL___SIR);
            this.pmd = pmg.getPopulationModelDefinition();
            this.pm = pmd.createModel();
            StlLoader stlLoader = new StlLoader(this.FORMULA_SIR);
            StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(getMeasuresMapFunction());
            this.formula = stlModelFactory.getQuantitativeMonitor("formula_id", new double[]{});
            this.setNumberOfTrajectories(100);
        }

        public void setNumberOfTrajectories(int n){
            this.numberOfTrajectories = n;
        }


        private Map<String, ToDoubleFunction<PopulationState>> getMeasuresMapFunction(){
            Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
            measuresMapping.put("#S", s -> s.getOccupancy(0));
            measuresMapping.put("#I", s -> s.getOccupancy(1));
            measuresMapping.put("#R", s -> s.getOccupancy(2));
            return measuresMapping;
        }

        private PopulationState getInitialState(){
            return new PopulationState(
                    3,
                    new Population(0,95),
                    new Population(1,5),
                    new Population(2,0)
            );
        }

        public void setParameters(Map<String,Double> parameters){
            for (String key : parameters.keySet()){
                this.pmd.setParameter(key,new SibillaDouble(parameters.get(key)));
            }
            this.pm = this.pmd.createModel();
        }


        private Trajectory<PopulationState> sampleATrajectory(){
            Trajectory<PopulationState> trajectory= this.se.sampleTrajectory(
                    this.rg,
                    this.pm,
                    this.getInitialState(),
                    this.formula.getTimeHorizon());
            double timeHorizon = formula.getTimeHorizon();
            trajectory.setEnd(timeHorizon);
            return trajectory;
        }


        public ToDoubleFunction<Map<String,Double>> getAverageRobustnessFunction(){
            return m -> {
                this.setParameters(m);
                return this.averageRobustness();
            };
        }

        public ToDoubleFunction<Map<String,Double>> getProbReachFunction(){
            return m -> {
                this.setParameters(m);
                return this.probReachFormula();
            };
        }

        public double averageRobustness(){
            double r = 0;
            for (int i = 0; i < this.numberOfTrajectories; i++) {
                r += formula.monitor(sampleATrajectory()).valueAt(0);
            }
            return  r/this.numberOfTrajectories;
        }

        public double probReachFormula(){
            double[] probabilities = QuantitativeMonitor
                    .computeProbability(
                            this.formula,
                            this::sampleATrajectory,
                            this.numberOfTrajectories,
                            new double[]{0.0}
                    );
            return probabilities[0];
        }
    }



    /**
     *
     * Refer to <a href="https://arxiv.org/pdf/1402.1450.pdf">the case</a>
     * k_i  [0.005, 0.3]
     * k_r  [0.005, 0.2]
     */

    @Disabled
    @Test
    public void testDatasetForSurrogateSir() throws StlModelGenerationException, ModelGenerationException, IOException {
        SirExample sirExample = new SirExample();

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_i",0.005,0.3),
                new ContinuousInterval("k_r",0.005,0.2));

        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),15, sirExample.getProbReachFunction());
        DataSet dataSetAvgRobust = new DataSet(hr, new FullFactorialSamplingTask(),15, sirExample.getAverageRobustnessFunction());

//        CSVWriter writer = new CSVWriter("/Users/lorenzomatteucci/Documents/test_stl_optimization", "", "");
//        writer.write("rsProb",dataSetProbReach);
//        writer.write("rsRob",dataSetAvgRobust);

    }

}
