package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisRecord;
import it.unicam.quasylab.sibilla.tools.synthesis.Synthesizer;


import java.util.Map;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.core.runtime.synthesis.Common.getAsMap;

public class OptimalFeasibilityTask extends SynthesisTask {

        private ObjectiveType objectiveType;
        private EvaluationType evaluationType;
        private ModelSpecs modelSpecs;
        private String formulae;
        private String formulaName;
        private EvaluationType.STLEvaluation evaluationFunction;

        public OptimalFeasibilityTask(Map<String, Object> taskMap,SynthesisStrategy synthesisStrategy) {
            super(taskMap,synthesisStrategy);
            Map<String, Object> taskSpecs = getAsMap(taskMap.get("taskSpecs"));
            this.setObjectiveType(taskSpecs);
            this.setEvaluationType(taskSpecs);
            this.setModelSpecs(taskSpecs);
            this.setFormulae(taskSpecs);
        }

        private void setObjectiveType(Map<String, Object> taskSpecs) {
            if (!taskSpecs.containsKey("objective")) {
                throw new IllegalArgumentException("Objective not found in taskSpecs!");
            }
            this.objectiveType = ObjectiveType.valueOf(taskSpecs.get("objective").toString().toUpperCase());
        }

        private void setEvaluationType(Map<String, Object> taskSpecs) {
            if (!taskSpecs.containsKey("evaluation")) {
                throw new IllegalArgumentException("Evaluation type not found in taskSpecs!");
            }
            this.evaluationType = EvaluationType.valueOf(taskSpecs.get("evaluation").toString().toUpperCase());
            this.evaluationFunction = evaluationType.getEvaluationFunction();
        }

        private void setModelSpecs(Map<String, Object> taskSpecs) {
            if (!taskSpecs.containsKey("model")) {
                throw new IllegalArgumentException("Model not found in taskSpecs!");
            }
            Map<String, Object> modelSpec = getAsMap(taskSpecs.get("model"));
            String module = (String) modelSpec.get("module");
            String initialConfiguration = (String) modelSpec.get("initialConfiguration");
            String modelSpecification = (String) modelSpec.get("modelSpecification");

            if (module == null || initialConfiguration == null || modelSpecification == null) {
                throw new IllegalArgumentException("Model must contain module, initialConfiguration, and modelSpecification!");
            }
            if(!super.isModuleAvailable(module))
                throw new IllegalArgumentException(String.format(super.UNKNOWN_MODULE_MESSAGE,module));

            this.modelSpecs = new ModelSpecs(module, initialConfiguration, modelSpecification);
        }

        private void setFormulae(Map<String, Object> taskSpecs) {
            if (!taskSpecs.containsKey("formulae"))
                throw new IllegalArgumentException("Formulae not found in taskSpecs!");
            this.formulae = taskSpecs.get("formulae").toString();

        }

        @Override
        public SynthesisRecord execute(SibillaRuntime runtime) throws CommandExecutionException, StlModelGenerationException {
            runtime.reset();
            runtime.loadModule(modelSpecs.module());
            runtime.load(modelSpecs.modelSpecification());
            runtime.setReplica(super.replica);
            runtime.setConfiguration(modelSpecs.initialConfiguration);
            runtime.loadFormula(getFormulae());
            this.formulaName = runtime.getMonitors().keySet().toArray(new String[0])[0];
            ToDoubleFunction<Map<String,Double>> objectiveFunction = getObjectiveFunction(runtime);
            Synthesizer synthesizer = generateSynthesizer(objectiveFunction, objectiveType.isMinimize());
            synthesizer.searchOptimalSolution();
            return synthesizer.getLastSynthesisRecord();
        }

        private ToDoubleFunction<Map<String,Double>> getObjectiveFunction(SibillaRuntime runtime) {
            return m->{
                setRuntimeParameters(runtime,m);
                double eval;
                try {
                    runtime.setConfiguration(modelSpecs.initialConfiguration());
                    eval = evaluationFunction.applyAsDouble(runtime,formulaName,new double[]{});
                } catch (CommandExecutionException | StlModelGenerationException e) {
                    throw new RuntimeException(e);
                }
                return eval;
            };
        }



    private record ModelSpecs(String module, String initialConfiguration, String modelSpecification) {}

        private enum ObjectiveType {
            MAXIMIZE, MINIMIZE;
            public boolean isMinimize(){
                return this == MINIMIZE;
            }
        }

        private enum EvaluationType {
            QUANTITATIVE, QUALITATIVE;

            public STLEvaluation getEvaluationFunction() {
                return switch (this) {
                    case QUALITATIVE -> SibillaRuntime::expectedProbabilityAtTime0;
                    case QUANTITATIVE -> SibillaRuntime::meanRobustnessAtTime0;
                };
            }

            @FunctionalInterface
            public interface STLEvaluation {
                double applyAsDouble(SibillaRuntime sr, String formulaName, double[] formulaParameters) throws CommandExecutionException, StlModelGenerationException;
            }
        }


        public String getFormulae() {
            return formulae;
        }

    @Override
    public String toString() {
        return "OptimalFeasibilityTask{" +
                "objective=" + objectiveType +
                ", evaluation=" + evaluationType +
                ", modelSpecs=" + modelSpecs +
                ", formulae='" + formulae + '\'' +
                '}';
    }
}
