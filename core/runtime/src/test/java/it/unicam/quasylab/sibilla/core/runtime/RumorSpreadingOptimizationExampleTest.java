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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;


public class RumorSpreadingOptimizationExampleTest {

    private static class RumorSpreadingExample{

        final private SimulationEnvironment se;
        final private RandomGenerator rg;
        final private PopulationModelDefinition pmd;
        private PopulationModel pm;
        final private QuantitativeMonitor<PopulationState> formula;

        private int numberOfTrajectories;

        final String FORMULA_RS =
                """
                measure #spreader;
                measure #ignorant;
                formula formula_id [] : ( \\G[3,5]( [#ignorant > 0]) ) && ( \\E[0,1]( \\G[0,0.02]( [#spreader > 50])) ) ;
                """;
        final String MODEL___RS =
                """
                        param k_s = 0.05;
                        param k_r = 0.05;
                                        
                        const initial_spreaders = 90;
                        const initial_ignorants = 10;
                        const initial_blockers = 0;
                                                    
                        species spreader;
                        species ignorant;
                        species blocker;
                                                    
                        rule spreading {
                            spreader|ignorant -[ #spreader * %ignorant * k_s ]-> spreader|spreader
                        }
                                                    
                        rule stop_spreading_1 {
                            spreader|spreader -[ #spreader * k_r ]-> spreader|blocker
                        }
                        
                        rule stop_spreading_2 {
                            blocker|spreader -[ #blocker * %spreader * k_r ]-> blocker|blocker
                        }
                                                   
                        system init = spreader<initial_spreaders>|ignorant<initial_ignorants>|blocker<initial_blockers>;
                        """;

        public RumorSpreadingExample() throws ModelGenerationException, StlModelGenerationException {
            this.se = new SimulationEnvironment();
            this.rg = new DefaultRandomGenerator();
            PopulationModelGenerator pmg = new PopulationModelGenerator(this.MODEL___RS);
            this.pmd = pmg.getPopulationModelDefinition();
            this.pm = pmd.createModel();
            StlLoader stlLoader = new StlLoader(this.FORMULA_RS);
            StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(getMeasuresMapFunction());
            this.formula = stlModelFactory.getQuantitativeMonitor("formula_id", new double[]{});
            this.setNumberOfTrajectories(100);
        }

        public void setNumberOfTrajectories(int n){
            this.numberOfTrajectories = n;
        }


        private Map<String, ToDoubleFunction<PopulationState>> getMeasuresMapFunction(){
            Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
            measuresMapping.put("#spreader", s -> s.getOccupancy(0));
            measuresMapping.put("#ignorant", s -> s.getOccupancy(1));
            measuresMapping.put("#blocker", s -> s.getOccupancy(2));
            return measuresMapping;
        }

        private PopulationState getInitialState(){
            return new PopulationState(
                    3,
                    new Population(0,90),
                    new Population(1,10),
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
            trajectory.removeSampleOverTheEnd();
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
     * Refer to <a href="https://openportal.isti.cnr.it/data/2015/424157/2015_424157.postprint.pdf">the case</a>
     * k_s  [0.0001, 2.0]
     * k_r  [0.0001, 0.5]
     */
    @Test
    public void testDatasetForSurrogateSr() throws StlModelGenerationException, ModelGenerationException, IOException {
        RumorSpreadingExample rsExample = new RumorSpreadingExample();

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_s",0.0001,2.0),
                new ContinuousInterval("k_r",0.0001,0.5));

        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),15, rsExample.getProbReachFunction());
        DataSet dataSetAvgRobust = new DataSet(hr, new FullFactorialSamplingTask(),15, rsExample.getAverageRobustnessFunction());

//        CSVWriter writer = new CSVWriter("/Users/lorenzomatteucci/Documents/test_stl_optimization", "", "");
//        writer.write("rsProb",dataSetProbReach);
//        writer.write("rsRob",dataSetAvgRobust);

    }


}
