package it.unicam.quasylab.sibilla.core.runtime.synthesis;


import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToIntFunction;

class SynthesisEvaluatorTest {



    String exampleOptFeasibilityFull =  """
            synthesisStrategy:
              searchSpace:
                - parameterName: "x"
                  lowerBound: -1.0
                  upperBound: 1.0
                - parameterName: "y"
                  lowerBound: -1.0
                  upperBound: 1.0
              sampling:
                  name: "lhs"
                  datasetSize: 1000
              surrogate:
                  name: "rf"
                  trainPortion: 0.9
                  properties:
                    - name: "numberOfTrees"
                      value: "300"
                    - name: "maxDepth"
                      value: "200"
              optimization:
                name: "pso"
                constraints:
                  - constraint: "x + y <= 1.0"
                  - constraint: "x + y >= -0.5"
                properties:
                  - name: "iteration"
                    value: "1000"
                  - name: "particle_number"
                    value: "500"
              infill:
                threshold: 0.1
                maxIteration: 10
          
            synthesisTask:
              type: "optimalFeasibility"
            
              simulationSetting:
                replica: 10
                dt: 1.0
                deadline: 100.0
            
              taskSpecs:
                objective: "maximize"
                evaluation: "quantitative"
                model:
                  module: "population"
                  initialConfiguration: "initial"
                  modelSpecification: |
                    line 1
                    line 2
                    line 3
                    line 4
                    ...
                formulae: |
                    line 1
                    line 2
                    line 3
                    line 4
                    ...
            """;


    String exampleComparisonFull =  """
            synthesisStrategy:
              searchSpace:
                - parameterName: "x"
                  lowerBound: -1.0
                  upperBound: 1.0
                - parameterName: "y"
                  lowerBound: -1.0
                  upperBound: 1.0
              sampling:
                  name: "lhs"
                  datasetSize: 1000
              surrogate:
                  name: "rf"
                  trainPortion: 0.9
                  properties:
                    - name: "numberOfTrees"
                      value: "300"
                    - name: "maxDepth"
                      value: "200"
              optimization:
                name: "pso"
                constraints:
                  - constraint: "x + y <= 1.0"
                  - constraint: "x + y >= -0.5"
                properties:
                  - name: "iteration"
                    value: "1000"
                  - name: "particle_number"
                    value: "500"
              infill:
                threshold: 0.1
                maxIteration: 10
          
            synthesisTask:
              type: "comparative"
            
              taskSpecs:
                objective: "preserve"  # or "diverge" or "improve"
                distanceType: "minkowski"
                p : 3
                modelOriginal:
                  module: "population"
                  initialConfiguration: "initial"
                  modelSpecificationPath: "theActualPath"
                  modelSpecification: |
                    line 1
                    line 2
                    line 3
                    line 4
                    ...
                modelVariant:
                  module: "population"
                  initialConfiguration: "initial"
                  modelSpecification: |
                    line 1
                    line 2
                    line 3
                    line 4
                    ...
                formulae: |
                    line 1
                    line 2
                    line 3
                    line 4
                    ...
            """;

    @Disabled
    @Test
    void testComparison(){

        SynthesisEvaluator evaluator = new SynthesisEvaluator(exampleComparisonFull);
        SynthesisStrategy configuration = evaluator.getStrategy();
        SynthesisTask task = evaluator.getTask();
        System.out.println(configuration);
        System.out.println(task);

    }


    @Disabled
    @Test
    void testOptimalFeasibility() {

        SynthesisEvaluator evaluator = new SynthesisEvaluator(exampleOptFeasibilityFull);
        SynthesisStrategy configuration = evaluator.getStrategy();
        SynthesisTask task = evaluator.getTask();
        System.out.println(configuration);
        System.out.println(task);

    }

    @Disabled
    @Test
    void testOptimalRepressilatorParameters() throws CommandExecutionException, StlModelGenerationException {
        String spec = """
              synthesisStrategy:
                searchSpace:
                - parameterName: "k_i"
                  lowerBound: 0.005
                  upperBound: 0.3
                - parameterName: "k_r"
                  lowerBound: 0.005
                  upperBound: 0.2
              synthesisTask:
                type: "optimalFeasibility"
                taskSpecs:
                    objective: "maximize"
                    evaluation: "qualitative"
                    model:
                        module: "population"
                        initialConfiguration: "initial"
                        modelSpecification: |
                            param n = 2;         /* Hill coefficient n */
                            param KM = 40;       /* K_M*/
                            param tau_mRNA = 2;  /* mRNA half life */
                            param tau_prot = 10; /* protein half life */
                            param ps_a = 0.5;    /* promotor strength (repressed) ( tps_repr ) */
                            param ps_0 = 0.0005; /* promotor strength (full) ( tps_active ) */
              
                            const ln2 = 0.69314718056;
                            const beta = 0.2;
                            const alpha0 = 0.2164;
                            const alpha = 216.404;
                            const eff = 20;
                            const t_ave =  tau_mRNA / ln2;   /* average mRNA lifetime */
                            const kd_mRNA = ln2 / tau_prot;  /* mRNA decay rate */
                            const kd_prot = ln2 / tau_mRNA;  /* protein decay rate  */
                            const k_tl = eff / t_ave;        /* translation rate  */
                            const a_tr = (ps_a -ps_0)*60;    /* transcription rate  */
                            const a0_tr = ps_0 * 60;         /* transcription rate (repressed)   */
              
                            const startY = 20; /* Initial number of Y agents */
                            const startVOID = 1; /* Initial number of VOID agents */
              
                            species VOID;  /* To represent nothingness, fictitious species are created */
              
                            species PX; /*  protein produced by X */
                            species PY; /*  protein produced by Y */
                            species PZ; /*  protein produced by Z */
                            species X; /*  mRNA X (LacI) */
                            species Y; /*  mRNA Y (TetR) */
                            species Z; /*  mRNA Z (CI)*/
              
              
                            rule degradation_of_X_transcripts {
                                X|VOID -[ kd_mRNA * #X ]-> VOID
                            }
              
                            rule degradation_of_Y_transcripts {
                                Y|VOID -[ kd_mRNA * #Y ]-> VOID
                            }
              
                            rule degradation_of_Z_transcripts {
                                Z|VOID -[ kd_mRNA * #Z ]-> VOID
                            }
              
                            rule translation_of_X {
                                VOID -[ k_tl * #X ]-> VOID|PX
                            }
              
                            rule translation_of_Y {
                                VOID -[ k_tl * #Y ]-> VOID|PY
                            }
              
                            rule translation_of_Z {
                                VOID -[ k_tl * #Z ]-> VOID|PZ
                            }
              
                            rule degradation_of_X {
                                PX|VOID -[ kd_prot * #PX ]-> VOID
                            }
              
                            rule degradation_of_Y {
                                PY|VOID -[ kd_prot * #PY ]-> VOID
                            }
              
                            rule degradation_of_Z {
                                PZ|VOID -[ kd_prot * #PZ ]-> VOID
                            }
              
                            rule transcription_of_X {
                                VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PZ^n ) ) ]-> VOID|X
                            }
              
                            rule transcription_of_Y {
                                VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PX^n ) ) ]-> VOID|Y
                            }
              
                            rule transcription_of_Z {
                                VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PY^n ) ) ]-> VOID|Z
                            }
              
                            system initial = Y < startY >|VOID < startVOID >;
                    formulae: |
                        measure #I
                        formula formula_id  : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
              """;

        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
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

     */
    @Disabled
    @Test
    void testOptimalSIRParameters() throws CommandExecutionException, StlModelGenerationException {
        String spec = """
              synthesisStrategy:
                searchSpace:
                - parameterName: "k_i"
                  lowerBound: 0.005
                  upperBound: 0.3
                - parameterName: "k_r"
                  lowerBound: 0.005
                  upperBound: 0.2
              synthesisTask:
                type: "optimalFeasibility"
                taskSpecs:
                    objective: "maximize"
                    evaluation: "qualitative"
                    model:
                        module: "population"
                        initialConfiguration: "initial"
                        modelSpecification: |
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
                            system initial = S<90>|I<10>|R<0>;
                    formulae: |
                        measure #I
                        formula formula_id  : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
              """;

        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
    }


    @Disabled
    @Test
    void testOptimalSIRParametersWithPath() throws CommandExecutionException, StlModelGenerationException {


        String spec = """
              synthesisStrategy:
                searchSpace:
                - parameterName: "k_i"
                  lowerBound: 0.005
                  upperBound: 0.3
                - parameterName: "k_r"
                  lowerBound: 0.005
                  upperBound: 0.2
              synthesisTask:
                type: "optimalFeasibility"
                taskSpecs:
                    objective: "maximize"
                    evaluation: "qualitative"
                    model:
                        module: "population"
                        initialConfiguration: "initial"
                        modelSpecificationPath: "/Users/lorenzomatteucci/phd/main/sibilla/core/runtime/src/test/java/it/unicam/quasylab/sibilla/core/runtime/synthesis/exampleSIR.txt"
                    formulae: |
                        measure #I
                        formula formula_id  : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
              """;

        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
    }



    @Disabled
    @Test
    void testOptimalSIRInterval_1() throws CommandExecutionException, StlModelGenerationException {
        String spec = """
              synthesisStrategy:
                searchSpace:
                - parameterName: "a"
                  lowerBound: 0.0
                  upperBound: 150.0
                - parameterName: "b"
                  lowerBound: 0.0
                  upperBound: 60.0
              synthesisTask:
                type: "optimalFeasibility"
                taskSpecs:
                    objective: "maximize"
                    evaluation: "qualitative"
                    model:
                        module: "population"
                        initialConfiguration: "initial"
                        modelSpecification: |
                            param k_i = 0.2;
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
                            system initial = S<90>|I<10>|R<0>;
                    formulae: |
                        measure %I
                        formula formula_id [a=0,b=100] : ( \\E[a,(a+b)][ #I == 0] )&& (\\G[0,a][ #I > 0 ]) endformula
              """;

        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
    }


    @Disabled
    @Test
    void testOptimalSIRInterval_2() throws CommandExecutionException, StlModelGenerationException {
        String spec = """
              synthesisStrategy:
                searchSpace:
                - parameterName: "a"
                  lowerBound: 0.0
                  upperBound: 50.0
                - parameterName: "b"
                  lowerBound: 0.0
                  upperBound: 10.0
              synthesisTask:
                type: "optimalFeasibility"
                taskSpecs:
                    objective: "maximize"
                    evaluation: "quantitative"
                    model:
                        module: "population"
                        initialConfiguration: "initial"
                        modelSpecification: |
                            param k_i = 0.2;
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
                            system initial = S<90>|I<10>|R<0>;
                    formulae: |
                        measure %I
                        formula formula_id [a=0,b=100] : ( \\G[a,(a+b)][ %I > 0.1] ) endformula
              """;

        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
    }



    @Disabled
    @Test
    public void testMultiple() throws CommandExecutionException, StlModelGenerationException {
        System.out.println("PRESERVE");
        for (int i = 0; i < 5; i++) {
            System.out.println("_ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _");
            testComparisonPreserveTSP();
        }
        printSeparator();
        System.out.println("DIVERGE");
        for (int i = 0; i < 5; i++) {
            System.out.println("_ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _");
            testComparisonDivergeTSP();
        }
        printSeparator();
        System.out.println("IMPROVE");
        for (int i = 0; i < 5; i++) {
            System.out.println("_ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _");
            testComparisonImproveTSP();
        }

    }

    private void printSeparator(){
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");

    }

    @Disabled
    @Test
    void testComparisonPreserveTSP() throws CommandExecutionException, StlModelGenerationException {
        String spec = """
                   synthesisStrategy:
                     searchSpace:
                       - parameterName: "redFractionIn0"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn1"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn2"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn3"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn4"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "malFractionForEach"
                         lowerBound: 0.0
                         upperBound: 1.0
                     optimization:
                        name: "pso"
                        constraints:
                          - constraint: "redFractionIn0 + redFractionIn1 + redFractionIn2 +redFractionIn3 +redFractionIn4 <= 3.5"
                          - constraint: "redFractionIn0 + redFractionIn1 + redFractionIn2 +redFractionIn3 +redFractionIn4 >= 1.5"
                        properties:
                          - name: "iteration"
                            value: "1000"
          
                   synthesisTask:
                     type: "comparative"
          
                     simulationSetting:
                        replica: 50
         
                     taskSpecs:
                         objective: "preserve"  # or "diverge" or "improve"
                         distanceType: "euclidian"
                         modelOriginal:
                           module: "population"
                           initialConfiguration: "initial"
                           modelSpecification: |
                             species RED;
                             species BLUE;
                             species UNC;
                             species MAL;
          
                             const persuasion_rate = 1.0;
                             param becoming_malevolent = 0.1;
          
                             rule UNC_to_BLUE {
                               UNC-[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %BLUE ]-> BLUE
                             }
          
                             rule UNC_to_RED {
                               UNC -[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %RED ]-> RED
                             }
          
                             rule UNC_to_MAL  {
                               UNC-[#UNC * persuasion_rate * %MAL ]-> MAL
                             }
          
                             rule BLUE_to_UNC {
                               BLUE -[ (%MAL>=0.5? 0:1) * #BLUE * persuasion_rate * %RED ]-> UNC
                             }
          
                             rule RED_to_UNC {
                               RED -[ (%MAL>=0.5? 0:1) * #RED * persuasion_rate * %BLUE ]-> UNC
                             }
          
                             measure R = %RED;
                             measure B = %BLUE;
                             measure U = %UNC;
                             measure M = %MAL;
          
                             system initial = RED<10>|BLUE<10>|UNC<75>|MAL<5>;
                         modelVariant:
                           module: "population"
                           initialConfiguration: "initial"
                           modelSpecification: |
                             const K = 5;       /* length of the ring */
          
                             species RED of [0,K];
                             species BLUE of [0,K];
                             species UNC of [0,K];
                             species MAL of [0,K];
          
                             const persuasion_rate = 3.0; /* persuasion rate */
                             const becoming_malevolent = 0.1;
          
                             const totalRedAndBlue = 20;
                             const totalUncAndMal = 80;
          
                             param redFractionIn0 = 0.5;
                             param redFractionIn1 = 0.5;
                             param redFractionIn2 = 0.5;
                             param redFractionIn3 = 0.5;
                             param redFractionIn4 = 0.5;
         
                             const redQuantityIn0 = redFractionIn0 * totalRedAndBlue / K;
                             const redQuantityIn1 = redFractionIn1 * totalRedAndBlue / K;
                             const redQuantityIn2 = redFractionIn2 * totalRedAndBlue / K;
                             const redQuantityIn3 = redFractionIn3 * totalRedAndBlue / K;
                             const redQuantityIn4 = redFractionIn4 * totalRedAndBlue / K;
          
                             const blueQuantityIn0 = (1.0-redFractionIn0) * totalRedAndBlue / K;
                             const blueQuantityIn1 = (1.0-redFractionIn1) * totalRedAndBlue / K;
                             const blueQuantityIn2 = (1.0-redFractionIn2) * totalRedAndBlue / K;
                             const blueQuantityIn3 = (1.0-redFractionIn3) * totalRedAndBlue / K;
                             const blueQuantityIn4 = (1.0-redFractionIn4) * totalRedAndBlue / K;
          
                             param malFractionForEach = 0.0;
          
                             const uncQuantityInEach = (1.0-malFractionForEach) * totalUncAndMal /K;
                             const malQuantityInEach = malFractionForEach * totalUncAndMal /K;
         
                             rule UNC_to_BLUE for i in [0,K] {
                               UNC[i]-[ (%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> BLUE[i]
                             }
          
                             rule UNC_to_RED for i in [0,K] {
                               UNC[i]-[(%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> RED[i]
                             }
          
                             rule UNC_to_MAL for i in [0,K] {
                               UNC[i]-[#UNC[i] * persuasion_rate * %MAL[i] ]-> MAL[i]
                             }
          
                             rule BLUE_to_UNC for i in [0,K] {
                               BLUE[i]-[ (%MAL[i]>=0.5? 0:1) * #BLUE[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> UNC[i]
                             }
          
                             rule RED_to_UNC for i in [0,K] {
                               RED[i]-[ (%MAL[i]>=0.5? 0:1) * #RED[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> UNC[i]
                             }
          
                             measure R = %RED[0] + %RED[1] + %RED[2] + %RED[3] + %RED[4];
                             measure B = %BLUE[0] + %BLUE[1] + %BLUE[2] + %BLUE[3] + %BLUE[4];
                             measure U = %UNC[0] + %UNC[1] + %UNC[2] + %UNC[3] + %UNC[4];
                             measure M = %MAL[0] + %MAL[1] + %MAL[2] + %MAL[3] + %MAL[4];
          
                             system initial = RED[0]<redQuantityIn0>|RED[1]<redQuantityIn1>|RED[2]<redQuantityIn2>|RED[3]<redQuantityIn3>|RED[4]<redQuantityIn4>|BLUE[0]<blueQuantityIn0>|BLUE[1]<blueQuantityIn1>|BLUE[2]<blueQuantityIn2>|BLUE[3]<blueQuantityIn3>|BLUE[4]<blueQuantityIn4>|UNC[0]<uncQuantityInEach>|UNC[1]<uncQuantityInEach>|UNC[2]<uncQuantityInEach>|UNC[3]<uncQuantityInEach>|UNC[4]<uncQuantityInEach>|MAL[0]<malQuantityInEach>|MAL[1]<malQuantityInEach>|MAL[2]<malQuantityInEach>|MAL[3]<malQuantityInEach>|MAL[4]<malQuantityInEach>;
          
                         formulae: |
                           measure R
                           measure B
                           measure M
                           formula formula_stability [t=25.0] : \\E[0,t][ R >= 1.0 ]   endformula
                           formula formula_coherence [t=25.0] : ([ R > 3 * B ]&&(\\E[0,t][ B <= 0.0])) endformula
                           formula formula_red_pres [t=25.0] : (\\G[0,t][ R >= 0.5]) endformula
                           formula formula_mal_pres [t=25.0] : ((\\E[0, 1/4 * t]([M >= 0.01]))<->(\\G[3/4 * t,t]([M >= 0.25] && [M <= 0.75]))) endformula
          """;


        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
        printTSPResult(sr);
    }

    @Disabled
    @Test
    void testComparisonDivergeTSP() throws CommandExecutionException, StlModelGenerationException {
        String spec = """
                   synthesisStrategy:
                     searchSpace:
                       - parameterName: "redFractionIn0"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn1"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn2"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn3"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn4"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "malFractionForEach"
                         lowerBound: 0.0
                         upperBound: 1.0
                     optimization:
                        name: "pso"
                        constraints:
                          - constraint: "redFractionIn0 + redFractionIn1 + redFractionIn2 +redFractionIn3 +redFractionIn4 <= 3.5"
                          - constraint: "redFractionIn0 + redFractionIn1 + redFractionIn2 +redFractionIn3 +redFractionIn4 >= 1.5"
                        properties:
                          - name: "iteration"
                            value: "1000"
          
                   synthesisTask:
                     type: "comparative"
          
                     simulationSetting:
                        replica: 20
         
                     taskSpecs:
                         objective: "diverge"
                         distanceType: "euclidian"
                         modelOriginal:
                           module: "population"
                           initialConfiguration: "initial"
                           modelSpecification: |
                             species RED;
                             species BLUE;
                             species UNC;
                             species MAL;
          
                             const persuasion_rate = 1.0;
                             param becoming_malevolent = 0.1;
          
                             rule UNC_to_BLUE {
                               UNC-[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %BLUE ]-> BLUE
                             }
          
                             rule UNC_to_RED {
                               UNC -[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %RED ]-> RED
                             }
          
                             rule UNC_to_MAL  {
                               UNC-[#UNC * persuasion_rate * %MAL ]-> MAL
                             }
          
                             rule BLUE_to_UNC {
                               BLUE -[ (%MAL>=0.5? 0:1) * #BLUE * persuasion_rate * %RED ]-> UNC
                             }
          
                             rule RED_to_UNC {
                               RED -[ (%MAL>=0.5? 0:1) * #RED * persuasion_rate * %BLUE ]-> UNC
                             }
          
                             measure R = %RED;
                             measure B = %BLUE;
                             measure U = %UNC;
                             measure M = %MAL;
          
                             system initial = RED<10>|BLUE<10>|UNC<75>|MAL<5>;
                         modelVariant:
                           module: "population"
                           initialConfiguration: "initial"
                           modelSpecification: |
                             const K = 5;       /* length of the ring */
          
                             species RED of [0,K];
                             species BLUE of [0,K];
                             species UNC of [0,K];
                             species MAL of [0,K];
          
                             param persuasion_rate = 3.0; /* persuasion rate */
                             param becoming_malevolent = 0.1;
          
                             param totalRedAndBlue = 20;
                             param totalUncAndMal = 80;
          
                             param redFractionIn0 = 0.5;
                             param redFractionIn1 = 0.5;
                             param redFractionIn2 = 0.5;
                             param redFractionIn3 = 0.5;
                             param redFractionIn4 = 0.5;
          
                             const redQuantityIn0 = redFractionIn0 * totalRedAndBlue / K;
                             const redQuantityIn1 = redFractionIn1 * totalRedAndBlue / K;
                             const redQuantityIn2 = redFractionIn2 * totalRedAndBlue / K;
                             const redQuantityIn3 = redFractionIn3 * totalRedAndBlue / K;
                             const redQuantityIn4 = redFractionIn4 * totalRedAndBlue / K;
          
                             const blueQuantityIn0 = (1.0-redFractionIn0) * totalRedAndBlue / K;
                             const blueQuantityIn1 = (1.0-redFractionIn1) * totalRedAndBlue / K;
                             const blueQuantityIn2 = (1.0-redFractionIn2) * totalRedAndBlue / K;
                             const blueQuantityIn3 = (1.0-redFractionIn3) * totalRedAndBlue / K;
                             const blueQuantityIn4 = (1.0-redFractionIn4) * totalRedAndBlue / K;
          
                             param malFractionForEach = 0.0;
          
                             const uncQuantityInEach = (1.0-malFractionForEach) * totalUncAndMal /K;
                             const malQuantityInEach = malFractionForEach * totalUncAndMal /K;
          
          
                             rule UNC_to_BLUE for i in [0,K] {
                               UNC[i]-[ (%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> BLUE[i]
                             }
          
                             rule UNC_to_RED for i in [0,K] {
                               UNC[i]-[(%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> RED[i]
                             }
          
                             rule UNC_to_MAL for i in [0,K] {
                               UNC[i]-[#UNC[i] * persuasion_rate * %MAL[i] ]-> MAL[i]
                             }
          
                             rule BLUE_to_UNC for i in [0,K] {
                               BLUE[i]-[ (%MAL[i]>=0.5? 0:1) * #BLUE[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> UNC[i]
                             }
          
                             rule RED_to_UNC for i in [0,K] {
                               RED[i]-[ (%MAL[i]>=0.5? 0:1) * #RED[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> UNC[i]
                             }
          
                             measure R = %RED[0] + %RED[1] + %RED[2] + %RED[3] + %RED[4];
                             measure B = %BLUE[0] + %BLUE[1] + %BLUE[2] + %BLUE[3] + %BLUE[4];
                             measure U = %UNC[0] + %UNC[1] + %UNC[2] + %UNC[3] + %UNC[4];
                             measure M = %MAL[0] + %MAL[1] + %MAL[2] + %MAL[3] + %MAL[4];
          
                             system initial = RED[0]<redQuantityIn0>|RED[1]<redQuantityIn1>|RED[2]<redQuantityIn2>|RED[3]<redQuantityIn3>|RED[4]<redQuantityIn4>|BLUE[0]<blueQuantityIn0>|BLUE[1]<blueQuantityIn1>|BLUE[2]<blueQuantityIn2>|BLUE[3]<blueQuantityIn3>|BLUE[4]<blueQuantityIn4>|UNC[0]<uncQuantityInEach>|UNC[1]<uncQuantityInEach>|UNC[2]<uncQuantityInEach>|UNC[3]<uncQuantityInEach>|UNC[4]<uncQuantityInEach>|MAL[0]<malQuantityInEach>|MAL[1]<malQuantityInEach>|MAL[2]<malQuantityInEach>|MAL[3]<malQuantityInEach>|MAL[4]<malQuantityInEach>;
          
                         formulae: |
                           measure R
                           measure B
                           measure M
                           formula formula_stability [t=25] : \\E[0,t][ R >= 1.0 ]   endformula
                           formula formula_coherence [t=25] : ([ R > 3 * B ]&&(\\E[0,t][ B <= 0.0])) endformula
                           formula formula_red_pres [t=25] : (\\G[0,t][ R >= 0.5]) endformula
                           formula formula_mal_pres [t=25] : ((\\E[0, 1/4 * t]([M >= 0.01]))<->(\\G[3/4 * t,t]([M >= 0.25] && [M <= 0.75]))) endformula
          """;


        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
        printTSPResult(sr);
    }

    @Disabled
    @Test
    void testComparisonImproveTSP() throws CommandExecutionException, StlModelGenerationException {
        String spec = """
                   synthesisStrategy:
                     searchSpace:
                       - parameterName: "redFractionIn0"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn1"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn2"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn3"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "redFractionIn4"
                         lowerBound: 0.1
                         upperBound: 1.0
                       - parameterName: "malFractionForEach"
                         lowerBound: 0.0
                         upperBound: 1.0
                     optimization:
                        name: "pso"
                        constraints:
                          - constraint: "redFractionIn0 + redFractionIn1 + redFractionIn2 +redFractionIn3 +redFractionIn4 <= 3.5"
                          - constraint: "redFractionIn0 + redFractionIn1 + redFractionIn2 +redFractionIn3 +redFractionIn4 >= 1.5"
                        properties:
                          - name: "iteration"
                            value: "1000"
          
                   synthesisTask:
                     type: "comparative"
          
                     simulationSetting:
                        replica: 20
         
                     taskSpecs:
                         objective: "improve"
                         distanceType: "euclidian"
                         modelOriginal:
                           module: "population"
                           initialConfiguration: "initial"
                           modelSpecification: |
                             species RED;
                             species BLUE;
                             species UNC;
                             species MAL;
          
                             const persuasion_rate = 1.0;
                             param becoming_malevolent = 0.1;
          
                             rule UNC_to_BLUE {
                               UNC-[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %BLUE ]-> BLUE
                             }
          
                             rule UNC_to_RED {
                               UNC -[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %RED ]-> RED
                             }
          
                             rule UNC_to_MAL  {
                               UNC-[#UNC * persuasion_rate * %MAL ]-> MAL
                             }
          
                             rule BLUE_to_UNC {
                               BLUE -[ (%MAL>=0.5? 0:1) * #BLUE * persuasion_rate * %RED ]-> UNC
                             }
          
                             rule RED_to_UNC {
                               RED -[ (%MAL>=0.5? 0:1) * #RED * persuasion_rate * %BLUE ]-> UNC
                             }
          
                             measure R = %RED;
                             measure B = %BLUE;
                             measure U = %UNC;
                             measure M = %MAL;
          
                             system initial = RED<10>|BLUE<10>|UNC<75>|MAL<5>;
                         modelVariant:
                           module: "population"
                           initialConfiguration: "initial"
                           modelSpecification: |
                             const K = 5;       /* length of the ring */
          
                             species RED of [0,K];
                             species BLUE of [0,K];
                             species UNC of [0,K];
                             species MAL of [0,K];
          
                             param persuasion_rate = 3.0; /* persuasion rate */
                             param becoming_malevolent = 0.1;
          
                             param totalRedAndBlue = 20;
                             param totalUncAndMal = 80;
          
                             param redFractionIn0 = 0.5;
                             param redFractionIn1 = 0.5;
                             param redFractionIn2 = 0.5;
                             param redFractionIn3 = 0.5;
                             param redFractionIn4 = 0.5;
          
                             const redQuantityIn0 = redFractionIn0 * totalRedAndBlue / K;
                             const redQuantityIn1 = redFractionIn1 * totalRedAndBlue / K;
                             const redQuantityIn2 = redFractionIn2 * totalRedAndBlue / K;
                             const redQuantityIn3 = redFractionIn3 * totalRedAndBlue / K;
                             const redQuantityIn4 = redFractionIn4 * totalRedAndBlue / K;
          
                             const blueQuantityIn0 = (1.0-redFractionIn0) * totalRedAndBlue / K;
                             const blueQuantityIn1 = (1.0-redFractionIn1) * totalRedAndBlue / K;
                             const blueQuantityIn2 = (1.0-redFractionIn2) * totalRedAndBlue / K;
                             const blueQuantityIn3 = (1.0-redFractionIn3) * totalRedAndBlue / K;
                             const blueQuantityIn4 = (1.0-redFractionIn4) * totalRedAndBlue / K;
          
                             param malFractionForEach = 0.0;
          
                             const uncQuantityInEach = (1.0-malFractionForEach) * totalUncAndMal /K;
                             const malQuantityInEach = malFractionForEach * totalUncAndMal /K;
          
          
                             rule UNC_to_BLUE for i in [0,K] {
                               UNC[i]-[ (%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> BLUE[i]
                             }
          
                             rule UNC_to_RED for i in [0,K] {
                               UNC[i]-[(%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> RED[i]
                             }
          
                             rule UNC_to_MAL for i in [0,K] {
                               UNC[i]-[#UNC[i] * persuasion_rate * %MAL[i] ]-> MAL[i]
                             }
          
                             rule BLUE_to_UNC for i in [0,K] {
                               BLUE[i]-[ (%MAL[i]>=0.5? 0:1) * #BLUE[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> UNC[i]
                             }
          
                             rule RED_to_UNC for i in [0,K] {
                               RED[i]-[ (%MAL[i]>=0.5? 0:1) * #RED[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> UNC[i]
                             }
          
                             measure R = %RED[0] + %RED[1] + %RED[2] + %RED[3] + %RED[4];
                             measure B = %BLUE[0] + %BLUE[1] + %BLUE[2] + %BLUE[3] + %BLUE[4];
                             measure U = %UNC[0] + %UNC[1] + %UNC[2] + %UNC[3] + %UNC[4];
                             measure M = %MAL[0] + %MAL[1] + %MAL[2] + %MAL[3] + %MAL[4];
          
                             system initial = RED[0]<redQuantityIn0>|RED[1]<redQuantityIn1>|RED[2]<redQuantityIn2>|RED[3]<redQuantityIn3>|RED[4]<redQuantityIn4>|BLUE[0]<blueQuantityIn0>|BLUE[1]<blueQuantityIn1>|BLUE[2]<blueQuantityIn2>|BLUE[3]<blueQuantityIn3>|BLUE[4]<blueQuantityIn4>|UNC[0]<uncQuantityInEach>|UNC[1]<uncQuantityInEach>|UNC[2]<uncQuantityInEach>|UNC[3]<uncQuantityInEach>|UNC[4]<uncQuantityInEach>|MAL[0]<malQuantityInEach>|MAL[1]<malQuantityInEach>|MAL[2]<malQuantityInEach>|MAL[3]<malQuantityInEach>|MAL[4]<malQuantityInEach>;
          
                         formulae: |
                           measure R
                           measure B
                           measure M
                           formula formula_stability [t=25] : \\E[0,t][ R >= 1.0 ]   endformula
                           formula formula_coherence [t=25] : ([ R > 3 * B ]&&(\\E[0,t][ B <= 0.0])) endformula
                           formula formula_red_pres [t=25] : (\\G[0,t][ R >= 0.5]) endformula
                           formula formula_mal_pres [t=25] : ((\\E[0, 1/4 * t]([M >= 0.01]))<->(\\G[3/4 * t,t]([M >= 0.25] && [M <= 0.75]))) endformula
          """;


        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
        printTSPResult(sr);
    }


    public void printTSPResult(SynthesisRecord record){
        Map<String,Double> values = record.optimalCoordinates();
        printSolutionAsMap(values);
    }


    public void printSolutionAsMap(Map<String, Double> parameters) {

        int numberOfRedAndBlue = 20;
        int numberOfUncAndMal = 80;
        int k = 5;

        // Extract malInFrac directly since it doesn't depend on the red fractions
        double malInFrac = parameters.getOrDefault("malFractionForEach", 0.0);
        int quantityOfUncPerSpace = Math.toIntExact(Math.round((1 - malInFrac) * numberOfUncAndMal / k));
        int quantityOfMalPerSpace = Math.toIntExact(Math.round(malInFrac * numberOfUncAndMal / k));
        int totalUnc = quantityOfUncPerSpace * k;
        int totalMal = quantityOfMalPerSpace * k;

        // For RED and BLUE
        ToIntFunction<Double> redFromFrac = fraction -> Math.toIntExact(Math.round(fraction * numberOfRedAndBlue / k));
        ToIntFunction<Double> blueFromFrac = fraction -> Math.toIntExact(Math.round((1 - fraction) * numberOfRedAndBlue / k));

        // TreeMap to sort the parameters (if you need sorting)
        TreeMap<String, Double> sortedParameters = new TreeMap<>(parameters);

        int totalRed = 0;
        int totalBlue = 0;

        System.out.println("Quantities of RED, BLUE, UNC, and MAL");
        System.out.println("__________________________________________");
        double sumOfFracRed= 0.0;
        // Process RED and BLUE quantities
        for (Map.Entry<String, Double> entry : sortedParameters.entrySet()) {
            if (entry.getKey().startsWith("redFractionIn")) {
                String index = entry.getKey().substring(entry.getKey().length() - 1);
                double fraction = entry.getValue();
                sumOfFracRed += fraction;
                int red = redFromFrac.applyAsInt(fraction);
                int blue = blueFromFrac.applyAsInt(fraction);
                totalRed += red;
                totalBlue += blue;

                System.out.printf("#RED[%s] = %d             #BLUE[%s] = %d\n", index, red, index, blue);
            }
        }
        System.out.println("SUM OF RED IN FRACTION : "+sumOfFracRed);

        // Print the total quantities for RED and BLUE
        System.out.println("__________________________________________");
        System.out.printf("#TOTAL_RED = %d \n#TOTAL_BLUE = %d\n", totalRed, totalBlue);
        System.out.println("--------------------------------------------------");

        // Assuming the structure is uniform across segments, print UNC and MAL for each segment
        for (int i = 0; i < k; i++) {
            System.out.printf("#UNC[%d] = %d             #MAL[%d] = %d\n", i, quantityOfUncPerSpace, i, quantityOfMalPerSpace);
        }

        // Print the total quantities for UNC and MAL
        System.out.println("--------------------------------------------------");
        System.out.printf("#TOTAL_UNC = %d \n#TOTAL_MAL = %d\n", totalUnc, totalMal);
    }



    /**
     *
     * Refer to <a href="https://openportal.isti.cnr.it/data/2015/424157/2015_424157.postprint.pdf">the case</a>
     * k_s  [0.0001, 2.0]
     * k_r  [0.0001, 0.5]
     */
    @SuppressWarnings("all")
    void caseTostudyInFuture(){
        String rsFormulaSpecification= """
                measure #S
                measure #I
                formula formula_id [] : ( \\G[3,5]( [ #I > 0 ] ) )  && ( \\E[0,1] ( \\G[0,0.02] ( [#S > 50 ] ) ) )  endformula
                """;

        String rsModelSpecification = """
                param k_s = 0.05;
                param k_r = 0.05;

                species S; /* spreaders */
                species I; /* ignorants */
                species B; /* blockers  */


                rule spreading {
                    S|I -[ #S * #I * k_s  ]-> S|S
                }

                rule stop_spreading_1 {
                    S|S -[ #S * (#S - 1) * k_r ]-> S|B
                }

                rule stop_spreading_2 {
                    S|B -[ #B * #S * k_r  ]-> B|B
                }
                system initial = S<10>|I<90>|B<0>;
                
                """;

        String MODEL___RS =
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

        String TEST_TSP_BATTERY = """
            param meetRate = 1;
                                  
            param scale = 10;
                                
            const b_size = 10;  /* battery max capacity         */
            const f_size = 2;   /* active flag size on and off  */
                        
            const startRED = 10;
            const startBLUE = 10;
                        
            param recharge_rate = 1; 
            param reactivation_rate = 1;
            param deactivation_rate = 1;
                                  
            species BLUE of [0,b_size]*[0,f_size];
            species RED of [0,b_size]*[0,f_size];
                        
            rule BLUE_persuades_RED for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b>=1)) {
                BLUE[b,f] | RED[b,f] -[ #BLUE[b,f] * meetRate * %RED[b,f] ]-> BLUE[b-1,f]|BLUE[b-1,f]
            }
                                            
            rule RED_persuades_BLUE for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b>=1)){
                BLUE[b,f]|RED[b,f] -[#RED[b,f] * meetRate * %BLUE[b,f] ]-> RED[b-1,f]|RED[b-1,f]
            }
                                          
            rule BLUE_deactivation for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b<b_size)){
                BLUE[b,f] -[ deactivation_rate ]-> BLUE[b,0]
            }
                                             
            rule RED_deactivation for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b<b_size)){
                RED[b,f] -[ deactivation_rate ]-> RED[b,0]
            }
                                             
            rule BLUE_recharging for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b<b_size-1)){
                BLUE[b,f] -[ recharge_rate ]-> BLUE[b+1,f]
            }
                                           
            rule RED_recharging for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b<b_size-1)) {
                RED[b,f] -[ recharge_rate ]-> RED[b+1,f]
            }
                                           
            rule BLUE_reactivation for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b>0)){
                BLUE[b,f] -[ reactivation_rate ]-> BLUE[b,1]
            }
                                             
            rule RED_reactivation for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b>0)){
                RED[b,f] -[ reactivation_rate ]-> RED[b,1]
            }
                        
                        
            system fair = RED[9,1]<startRED>|BLUE[9,1]<startBLUE>;                      
                        
            system balanced = RED[b_size-1,1]<1*scale>|BLUE[b_size-1,1]<1*scale>;
            system favor_of_RED = RED[b_size-1,1]<2*scale>|BLUE[b_size-1,1]<1*scale>;
            system favor_of_BLUE = RED[b_size-1,1]<1*scale>|BLUE[b_size-1,1]<2*scale>;
                        
            predicate RED_WIN = ( %RED[0,0] + %RED[1,0] + %RED[2,0] + %RED[3,0] + %RED[4,0] + %RED[5,0] + %RED[6,0] + %RED[7,0] + %RED[8,0] + %RED[9,0] + %RED[0,1] + %RED[1,1] +  %RED[2,1] + %RED[3,1] + %RED[4,1] + %RED[5,1] + %RED[6,1] + %RED[7,1] + %RED[8,1] + %RED[9,1] ) > 0.99 ;
            predicate BLUE_WIN = ( %BLUE[0,0] + %BLUE[1,0] + %BLUE[2,0] + %BLUE[3,0] + %BLUE[4,0] + %BLUE[5,0] + %BLUE[6,0] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %BLUE[0,1] + %BLUE[1,1] +  %BLUE[2,1] + %BLUE[3,1] + %BLUE[4,1] + %BLUE[5,1] + %BLUE[6,1] + %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1]) > 0.99 ;         
            predicate consensus = ((( %RED[0,0] + %RED[1,0] + %RED[2,0] + %RED[3,0] + %RED[4,0] + %RED[5,0] + %RED[6,0] + %RED[7,0] + %RED[8,0] + %RED[9,0] + %RED[0,1] + %RED[1,1] +  %RED[2,1] + %RED[3,1] + %RED[4,1] + %RED[5,1] + %RED[6,1] + %RED[7,1] + %RED[8,1] + %RED[9,1] ) > 0.99 ) || (( %BLUE[0,0] + %BLUE[1,0] + %BLUE[2,0] + %BLUE[3,0] + %BLUE[4,0] + %BLUE[5,0] + %BLUE[6,0] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %BLUE[0,1] + %BLUE[1,1] +  %BLUE[2,1] + %BLUE[3,1] + %BLUE[4,1] + %BLUE[5,1] + %BLUE[6,1] + %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1]) > 0.99)); /* predicate consensus = ( (RED_WIN) || (BLUE_WIN) ); */
            predicate consensus_and_charged = ((( %RED[0,0] + %RED[1,0] + %RED[2,0] + %RED[3,0] + %RED[4,0] + %RED[5,0] + %RED[6,0] + %RED[7,0] + %RED[8,0] + %RED[9,0] + %RED[0,1] + %RED[1,1] +  %RED[2,1] + %RED[3,1] + %RED[4,1] + %RED[5,1] + %RED[6,1] + %RED[7,1] + %RED[8,1] + %RED[9,1] ) > 0.99 ) || (( %BLUE[0,0] + %BLUE[1,0] + %BLUE[2,0] + %BLUE[3,0] + %BLUE[4,0] + %BLUE[5,0] + %BLUE[6,0] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %BLUE[0,1] + %BLUE[1,1] +  %BLUE[2,1] + %BLUE[3,1] + %BLUE[4,1] + %BLUE[5,1] + %BLUE[6,1] + %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1]) > 0.99)) && (( %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %RED[7,1] + %RED[8,1] + %RED[9,1] + %RED[7,0] + %RED[8,0] + %RED[9,0] )>0.7);
            """;

         String TEST_THE_THING = """
            param infection_rate = 1.0;
            param paranoia = 1.0;
            param meet_rate = 1.0;
            param moral_decay_rate = 0.25;
            param suicide_rate = 0.05;
            
            const max_sanity = 3;
            
            species human of [0,max_sanity];
            species infected_human;
            species deceased;
            
            rule loss_of_sanity for i in [0,max_sanity] when i>0 {
                human[i] -[ moral_decay_rate ]-> human[i-1]
            }
            
            rule moral_support for i in [0,max_sanity] and j in [0,max_sanity]  when ((i<max_sanity-1)&&(j<max_sanity-1)) {
                human[i]|human[j] -[ (#human[0] + #human[1] + #human[2]) * meet_rate ]-> human[i+1]|human[j+1]
            }
            
            rule human_get_infected for i in [0,max_sanity-1] {
                human[i]|infected_human -[ (#human[0] + #human[1] + #human[2]) * %infected_human * meet_rate ]-> infected_human|infected_human
            }
            
            rule killing_human for i in [0,max_sanity-1] {
                human[i]|human[i] -[ (#human[0] + #human[1] + #human[2]) * meet_rate * paranoia ]-> human[i]|deceased
            }
                        
            rule killing_infected_human for i in [0,max_sanity-1] {
                human[i]|infected_human -[ (#human[0] + #human[1] + #human[2]) * %infected_human * meet_rate * paranoia  ]-> human[i]|deceased
            }
                        
            rule committing_suicide for i in [0,max_sanity] when i==0 {
                human[i] -[ suicide_rate ]-> deceased
            }
                        
            system initial = human[max_sanity-1]<10>|infected_human<3>;
                        
            predicate alien_eradicated = (%infected_human == 0.0);
            predicate most_humans_survived = ((%human[0] + %human[1] + %human[2])>=0.75);
            predicate half_humans_survived_and_alien_eradicated = ((%human[0] + %human[1] + %human[2])>=0.75) && (%infected_human == 0.0);
            """;

        String TEST_FUNGI = """
            /* VARIABLES  */
                        
            param t = 19; /* current temperature */
            param h = 0.5;  /*  current humidity  */
                        
            /* CONSTANTS */
                        
            const e  = 2.7182818284590452353602874713527; /* euler number */
                        
            /* A */
                        
            const ideal_t_A = 15;
            const ideal_h_A = 0.7;
            const var_t_A = 1.6;
            const var_h_A = 0.8;
                        
            /* B */
                        
            const ideal_t_B = 22;
            const ideal_h_B = 0.4;
            const var_t_B = 1.0;
            const var_h_B = 0.7;
                        
            /* Common */
                        
            const interaction_rate = 0.005;
                        
            /* SPECIES */
                        
            /* species VOID;  To represent nothingness */
            species A;  /* Fungi type A */
            species B;  /* Fungi type B */
                        
            /* Reproduction rules */
                        
            rule reproduction_of_A {
                A -[  %A * (e^(-1*(( ideal_t_A - t )/(var_t_A))^2)  *  e^(-1*(( ideal_h_A - h )/(var_h_A))^2))  ]-> A|A
            }
                        
            rule reproduction_B {
                B -[  %B * (e^(-1*(( ideal_t_B - t )/(var_t_B))^2)  *  e^(-1*(( ideal_h_B - h )/(var_h_B))^2)) ]-> B|B
            }
                        
                        
            /* Killing rules */
                        
            rule A_kill_B {
                A|B -[ %A * interaction_rate * %B ]-> A
            }
                        
            rule B_kill_A {
                A|B -[ %B * interaction_rate * %A ]-> B
            }
                        
            /* SYSTEM */
                        
            system fair = A<10>|B<10>;
                        
            /* PREDICATE */
                        
            predicate balanced = (%A >= 0.45) && (%B >= 0.45) && ((#A+#B)>=15);
                        
            predicate majorityA = (%A >= 0.65) && (%B >= 0.25) && (#B>0);
            predicate majorityB = (%B >= 0.65) && (%A >= 0.25);
                        
            predicate onlyA = (%A == 1.0);
            predicate onlyB = (%B == 1.0);
            """;




    }


}