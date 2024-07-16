package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
            
              simulationSetting:
                replica: 10
                dt: 1.0
                deadline: 100.0
            
              taskSpecs:
                objective: "preserve"  # or "diverge" or "improve"
                distanceType: "minkowski"
                p : 3
                modelOriginal:
                  module: "population"
                  initialConfiguration: "initial"
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

    @Test
    void testComparison(){

        SynthesisEvaluator evaluator = new SynthesisEvaluator(exampleComparisonFull);
        SynthesisStrategy configuration = evaluator.getStrategy();
        SynthesisTask task = evaluator.getTask();
        System.out.println(configuration);
        System.out.println(task);

    }


    @Test
    void testOptimalFeasibility(){

        SynthesisEvaluator evaluator = new SynthesisEvaluator(exampleOptFeasibilityFull);
        SynthesisStrategy configuration = evaluator.getStrategy();
        SynthesisTask task = evaluator.getTask();
        System.out.println(configuration);
        System.out.println(task);

    }


    @Disabled
    @Test
    void testOptimalSIR() throws CommandExecutionException, StlModelGenerationException {
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
                        formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
              """;

        SynthesisEvaluator evaluator = new SynthesisEvaluator(spec);
        SynthesisTask task = evaluator.getTask();

        SynthesisRecord sr = task.execute(new SibillaRuntime());
        System.out.println(sr.info(false));
    }


}