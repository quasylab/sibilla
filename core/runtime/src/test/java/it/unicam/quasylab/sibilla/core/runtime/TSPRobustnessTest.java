package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.Signal;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import it.unicam.quasylab.sibilla.langs.stl.StlLoader;
import it.unicam.quasylab.sibilla.langs.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.langs.stl.StlMonitorFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;


public class TSPRobustnessTest {

    private SimulationEnvironment se;
    private RandomGenerator rg;

    private String getFormulaCoherence(){
        return """
                measure %RED
                measure %BLUE
                formula formula_coherence [] : ([%RED> 3 * %BLUE]&&(\\E[0,25][%RED>= 1.0])) endformula
                """;
    }
    private String getFormulaStability(){
        return """
                measure %RED
                measure %BLUE
                formula formula_stability [] : \\E[0,25][ %RED >= 1.0 ]  || \\E[0,25][ %BLUE >= 1.0 ]   endformula
                """;

    }

    private String getFormulaRedPresence(){
        return """
                measure %RED
                measure %BLUE
                formula formula_red [] : (\\G[0,25][%RED >= 0.5]) endformula
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


//    private String getTspRingMalString(){
//        return """
//                const K = 5;       /* length of the ring */
//
//                species RED of [0,K];
//                species BLUE of [0,K];
//                species UNC of [0,K];
//                species MAL of [0,K];
//
//                param persuasion_rate = 3.0; /* persuasion rate */
//                param becoming_malevolent = 0.01;
//
//
//                rule UNC_to_BLUE for i in [0,K] {
//                    UNC[i]-[ (%MAL>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> BLUE[i]
//                }
//
//                rule UNC_to_RED for i in [0,K] {
//                    UNC[i]-[(%MAL>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> RED[i]
//                }
//
//                rule UNC_to_MAL for i in [0,K] {
//                    UNC[i]-[#UNC[i] * becoming_malevolent ]-> MAL[i]
//                }
//
//                rule BLUE_to_UNC for i in [0,K] {
//                    BLUE[i]-[ (%MAL>=0.5? 0:1) * #BLUE[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> UNC[i]
//                }
//
//                rule RED_to_UNC for i in [0,K] {
//                    RED[i]-[ (%MAL>=0.5? 0:1) * #RED[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> UNC[i]
//                }
//
//
//                measure redPercentage = %RED[0] + %RED[1] + %RED[2] + %RED[3] + %RED[4];
//                measure bluePercentage = %BLUE[0] + %BLUE[1] + %BLUE[2] + %BLUE[3] + %BLUE[4];
//                measure uncPercentage = %UNC[0] + %UNC[1] + %UNC[2] + %UNC[3] + %UNC[4];
//
//                measure redQuantity = #RED[0] + #RED[1] + #RED[2] + #RED[3] + #RED[4];
//                measure blueQuantity = #BLUE[0] + #BLUE[1] + #BLUE[2] + #BLUE[3] + #BLUE[4];
//                measure uncQuantity = #UNC[0] + #UNC[1] + #UNC[2] + #UNC[3] + #UNC[4];
//
//                system balanced = RED[0]<2>|RED[1]<2>|RED[2]<2>|RED[3]<2>|RED[4]<2>|BLUE[0]<2>|BLUE[1]<2>|BLUE[2]<2>|BLUE[3]<2>|BLUE[4]<2>|UNC[0]<16>|UNC[1]<16>|UNC[2]<16>|UNC[3]<16>|UNC[4]<16>;
//                system red_advantage = RED[0]<3>|RED[1]<3>|RED[2]<3>|RED[3]<3>|RED[4]<3>|BLUE[0]<1>|BLUE[1]<1>|BLUE[2]<1>|BLUE[3]<1>|BLUE[4]<1>|UNC[0]<16>|UNC[1]<16>|UNC[2]<16>|UNC[3]<16>|UNC[4]<16>;
//                system blue_advantage = RED[0]<1>|RED[1]<1>|RED[2]<1>|RED[3]<1>|RED[4]<1>|BLUE[0]<3>|BLUE[1]<3>|BLUE[2]<3>|BLUE[3]<3>|BLUE[4]<3>|UNC[0]<16>|UNC[1]<16>|UNC[2]<16>|UNC[3]<16>|UNC[4]<16>;
//
//
//                """;
//    }
    private Map<String, ToDoubleFunction<PopulationState>> getMeasureMapTSPCompartment(){
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

    private Trajectory<PopulationState> sampleATrajectory(PopulationModel currentModel, PopulationState currentInitialState, double timeHorizon){
        Trajectory<PopulationState> trajectory= se.sampleTrajectory(
                rg,
                currentModel,
                currentInitialState,
                timeHorizon);
        trajectory.setEnd(timeHorizon);
        return trajectory;
    }


    private PopulationState getInitialRingState(){
        return new PopulationState(new int[]{
                0,   //  0 - RED[0]
                1,   //  1 - RED[1]
                1,   //  2 - RED[2]
                0,   //  3 - RED[3]
                1,   //  4 - RED[4]
                4,   //  5 - BLUE[0]
                3,   //  6 - BLUE[1]
                3,   //  7 - BLUE[2]
                4,   //  8 - BLUE[3]
                3,   //  9 - BLUE[4]
                16,  // 10 - UNC[0]
                16,  // 11 - UNC[1]
                16,  // 12 - UNC[2]
                16,  // 13 - UNC[3]
                16,  // 14 - UNC[4]
        });
    }


    private PopulationState getInitialConfig1(){
        return new PopulationState(new int[]{
                3,   //  0 - RED[0]
                3,   //  1 - RED[1]
                3,   //  2 - RED[2]
                3,   //  3 - RED[3]
                3,   //  4 - RED[4]
                1,   //  5 - BLUE[0]
                1,   //  6 - BLUE[1]
                1,   //  7 - BLUE[2]
                1,   //  8 - BLUE[3]
                1,   //  9 - BLUE[4]
                16,  // 10 - UNC[0]
                16,  // 11 - UNC[1]
                16,  // 12 - UNC[2]
                16,  // 13 - UNC[3]
                16,  // 14 - UNC[4]
        });
    }

    private PopulationState getInitialConfig2(){
        return new PopulationState(new int[]{
                4,   //  0 - RED[0]
                4,   //  1 - RED[1]
                4,   //  2 - RED[2]
                2,   //  3 - RED[3]
                1,   //  4 - RED[4]
                0,   //  5 - BLUE[0]
                0,   //  6 - BLUE[1]
                0,   //  7 - BLUE[2]
                2,   //  8 - BLUE[3]
                3,   //  9 - BLUE[4]
                16,  // 10 - UNC[0]
                16,  // 11 - UNC[1]
                16,  // 12 - UNC[2]
                16,  // 13 - UNC[3]
                16,  // 14 - UNC[4]
        });
    }

    private PopulationState getInitialConfig3(){
        return new PopulationState(new int[]{
                15,   //  0 - RED[0]
                0,   //  1 - RED[1]
                0,   //  2 - RED[2]
                0,   //  3 - RED[3]
                0,   //  4 - RED[4]
                1,   //  5 - BLUE[0]
                1,   //  6 - BLUE[1]
                1,   //  7 - BLUE[2]
                1,   //  8 - BLUE[3]
                1,   //  9 - BLUE[4]
                16,  // 10 - UNC[0]
                16,  // 11 - UNC[1]
                16,  // 12 - UNC[2]
                16,  // 13 - UNC[3]
                16,  // 14 - UNC[4]
        });
    }
    private PopulationState getInitialConfig4(){
        return new PopulationState(new int[]{
                15,   //  0 - RED[0]
                0,   //  1 - RED[1]
                0,   //  2 - RED[2]
                0,   //  3 - RED[3]
                0,   //  4 - RED[4]
                0,   //  5 - BLUE[0]
                0,   //  6 - BLUE[1]
                5,   //  7 - BLUE[2]
                0,   //  8 - BLUE[3]
                0,   //  9 - BLUE[4]
                16,  // 10 - UNC[0]
                16,  // 11 - UNC[1]
                16,  // 12 - UNC[2]
                16,  // 13 - UNC[3]
                16,  // 14 - UNC[4]
        });
    }

    private double robustness(PopulationModel currentModel, PopulationState currentInitialState, QuantitativeMonitor<PopulationState> currentMonitor, int numberOfTrj){
        double robustness = 0.0;
        for (int i = 0; i < numberOfTrj; i++) {
            Trajectory<PopulationState> trj = this.sampleATrajectory(currentModel,
                    currentInitialState,
                    currentMonitor.getTimeHorizon());
            Signal robustnessSignal = currentMonitor.monitor(trj);
            double currentRobustness = robustnessSignal.valueAt(0.0);
            robustness += currentRobustness;

        }
        return robustness / numberOfTrj;

    }



    public double getRobustnessCoherence() throws StlModelGenerationException {
        this.se = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        PopulationModel pm = getPopulationModel(getTspRingString());
        PopulationState initialState = getInitialRingState();
        QuantitativeMonitor<PopulationState> qm = getQuantitativeMonitor(getFormulaCoherence(),
                "formula_coherence",
                new double[]{},
                getMeasureMapTSPCompartment()
        );
        double robustness = robustness(pm,initialState,qm,100);
        return robustness;

    }


    public double getRobustnessStability() throws StlModelGenerationException {
        this.se = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        PopulationModel pm = getPopulationModel(getTspRingString());
        PopulationState initialState = getInitialRingState();
        QuantitativeMonitor<PopulationState> qm = getQuantitativeMonitor(getFormulaStability(),
                "formula_stability",
                new double[]{},
                getMeasureMapTSPCompartment()
        );
        double robustness = robustness(pm,initialState,qm,100);
        return robustness;

    }



    public double getRedPresence() throws StlModelGenerationException {
        this.se = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        PopulationModel pm = getPopulationModel(getTspRingString());
        PopulationState initialState = getInitialRingState();
        QuantitativeMonitor<PopulationState> qm = getQuantitativeMonitor(getFormulaRedPresence(),
                "formula_red",
                new double[]{},
                getMeasureMapTSPCompartment()
        );
        double robustness = robustness(pm,initialState,qm,100);
        return robustness;

    }








    public double getRobustnessCoherence(PopulationState initialState) throws StlModelGenerationException {
        this.se = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        PopulationModel pm = getPopulationModel(getTspRingString());
        QuantitativeMonitor<PopulationState> qm = getQuantitativeMonitor(getFormulaCoherence(),
                "formula_coherence",
                new double[]{},
                getMeasureMapTSPCompartment()
        );
        double robustness = robustness(pm,initialState,qm,100);
        return robustness;

    }


    public double getRobustnessStability(PopulationState initialState) throws StlModelGenerationException {
        this.se = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        PopulationModel pm = getPopulationModel(getTspRingString());
        QuantitativeMonitor<PopulationState> qm = getQuantitativeMonitor(getFormulaStability(),
                "formula_stability",
                new double[]{},
                getMeasureMapTSPCompartment()
        );
        double robustness = robustness(pm,initialState,qm,100);
        return robustness;

    }



    public double getRedPresence(PopulationState initialState) throws StlModelGenerationException {
        this.se = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        PopulationModel pm = getPopulationModel(getTspRingString());
        QuantitativeMonitor<PopulationState> qm = getQuantitativeMonitor(getFormulaRedPresence(),
                "formula_red",
                new double[]{},
                getMeasureMapTSPCompartment()
        );
        double robustness = robustness(pm,initialState,qm,100);
        return robustness;

    }



    public void checkForTheInitialConfig(PopulationState initial) throws StlModelGenerationException {
        System.out.println("\n STABILITY");
        System.out.println("    "+getRobustnessStability(initial));
        System.out.println("\n COHERENCE");
        System.out.println("    "+getRobustnessCoherence(initial));
        System.out.println("\n RED:");
        System.out.println("    "+getRedPresence(initial));
        System.out.println("..........................................");
    }
    @Disabled
    @Test
    public void testDifferentConfig() throws StlModelGenerationException {
        checkForTheInitialConfig(getInitialConfig1());
        checkForTheInitialConfig(getInitialConfig2());
        checkForTheInitialConfig(getInitialConfig3());
        checkForTheInitialConfig(getInitialConfig4());
    }


    @Disabled
    @Test
    public void checkRobustnessStabAndCoherence() throws StlModelGenerationException {
        System.out.println("\n STABILITY");
        System.out.println("    "+getRobustnessStability());
        System.out.println("\n COHERENCE");
        System.out.println("    "+getRobustnessCoherence());
        System.out.println("\n RED:");
        System.out.println("    "+getRedPresence());
    }



}
