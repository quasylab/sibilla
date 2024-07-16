package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisRecord;

import java.util.Map;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.core.runtime.synthesis.Common.getAsMap;

public class ComparisonTask extends SynthesisTask {

    ComparisonType comparisonType;
    DistanceType distanceType;
    int p;
    ModelSpecs modelOriginalSpecs;
    ModelSpecs modelVariantSpecs;
    String formulae;
    boolean isMinimization;


    public ComparisonTask(Map<String, Object> taskMap,SynthesisStrategy synthesisStrategy) {
        super(taskMap,synthesisStrategy);
        Map<String,Object> taskSpecs = getAsMap(taskMap.get("taskSpecs"));
        this.setComparisonType(taskSpecs);
        this.setDistanceType(taskSpecs);
        this.setModelOriginalSpecs(taskSpecs);
        this.setModelVariantSpecs(taskSpecs);
        this.setFormulae(taskSpecs);

    }

    private void setFormulae(Map<String, Object> taskSpecs) {
        if (!taskSpecs.containsKey("formulae"))
            throw new IllegalArgumentException("Formulae not found in taskSpecs!");
        this.formulae = taskSpecs.get("formulae").toString();
    }

    private void setModelVariantSpecs(Map<String, Object> taskSpecs) {
        if (!taskSpecs.containsKey("modelVariant")) {
            throw new IllegalArgumentException("modelVariant not found in taskSpecs!");
        }
        Map<String, Object> modelVariantSpec = getAsMap(taskSpecs.get("modelVariant"));
        String module = (String) modelVariantSpec.get("module");
        String initialConfiguration = (String) modelVariantSpec.get("initialConfiguration");
        String modelSpecification = (String) modelVariantSpec.get("modelSpecification");

        if (module == null || initialConfiguration == null || modelSpecification == null) {
            throw new IllegalArgumentException("modelVariant must contain module, initialConfiguration and modelSpecification!");
        }
        if(!super.isModuleAvailable(module))
            throw new IllegalArgumentException(String.format(super.UNKNOWN_MODULE_MESSAGE,module));

        this.modelVariantSpecs = new ModelSpecs(module, initialConfiguration, modelSpecification);
    }

    private void setModelOriginalSpecs(Map<String, Object> taskSpecs) {
        if (!taskSpecs.containsKey("modelOriginal")) {
            throw new IllegalArgumentException("modelOriginal not found in taskSpecs!");
        }
        Map<String, Object> modelOriginalSpec = getAsMap(taskSpecs.get("modelOriginal"));
        String module = (String) modelOriginalSpec.get("module");
        String initialConfiguration = (String) modelOriginalSpec.get("initialConfiguration");
        String modelSpecification = (String) modelOriginalSpec.get("modelSpecification");

        if (module == null || initialConfiguration == null || modelSpecification == null) {
            throw new IllegalArgumentException("modelOriginal must contain module, initialConfiguration, and modelSpecification!");
        }

        this.modelOriginalSpecs = new ModelSpecs(module, initialConfiguration, modelSpecification);
    }


    private void setComparisonType(Map<String,Object> taskSpecs){
        if(!taskSpecs.containsKey("objective"))
            throw new IllegalArgumentException("Objective not found!");
        comparisonType = ComparisonType.getValueOf(taskSpecs.get("objective").toString());
    }

    private void setDistanceType(Map<String,Object> taskSpecs){

        distanceType = taskSpecs.containsKey("distance") ? DistanceType.getValueOf(taskSpecs.get("distance").toString())
                : DistanceType.EUCLIDEAN;
        p = taskSpecs.containsKey("p") ? (int) taskSpecs.get("p") : 2;

    }


    private String[] getFormulaNames(SibillaRuntime sr) throws StlModelGenerationException {
        return sr.getMonitors().keySet().toArray(new String[0]);
    }

    private double[] evaluateOriginalRobustnessVector(SibillaRuntime sr) throws CommandExecutionException, StlModelGenerationException {
        sr.loadModule(modelOriginalSpecs.modelSpecification);
        sr.load(modelOriginalSpecs.modelSpecification);
        sr.setDt(super.dt);
        sr.setReplica(super.replica);
        sr.setConfiguration(modelOriginalSpecs.initialConfiguration);
        sr.loadFormula(formulae);

        String[] formulaNames = getFormulaNames(sr);
        double[] originalRobustnessVector = new double[formulaNames.length];

        for (int i = 0; i < formulaNames.length; i++) {
            originalRobustnessVector[i] = sr.meanRobustnessAtTime0(formulaNames[i], new double[]{}); //TODO
        }

        return originalRobustnessVector;
    }

    private double[] evaluateVariantRobustnessVector(SibillaRuntime sr) throws CommandExecutionException, StlModelGenerationException {
        sr.setConfiguration(modelVariantSpecs.initialConfiguration);
        String[] formulaNames = getFormulaNames(sr);
        double[] variantRobustnessVector = new double[formulaNames.length];
        for (int i = 0; i < formulaNames.length; i++) {
            variantRobustnessVector[i] = sr.meanRobustnessAtTime0(formulaNames[i], new double[]{}); //TODO
        }
        return variantRobustnessVector;
    }


    @Override
    public SynthesisRecord execute(SibillaRuntime runtime) throws CommandExecutionException, StlModelGenerationException {
        runtime.reset();
        double[]  originalRobustnessVector = evaluateOriginalRobustnessVector(runtime);
        runtime.reset();
        runtime.loadModule(modelVariantSpecs.modelSpecification);
        runtime.load(modelVariantSpecs.modelSpecification);
        runtime.load(formulae);
        ToDoubleFunction<Map<String,Double>> objFunction = getObjectiveFunction(runtime, originalRobustnessVector);
        return generateSynthesizer(objFunction, this.isMinimization).getLastSynthesisRecord();
    }


    private ToDoubleFunction<Map<String,Double>> getObjectiveFunction(SibillaRuntime runtime, double[] originalRobustnessVector) {
        return m -> {
            setRuntimeParameters(runtime,m);
            double[] variantRobustnessVector;
            try {
                variantRobustnessVector = evaluateVariantRobustnessVector(runtime);

            } catch (CommandExecutionException | StlModelGenerationException e) {
                throw new RuntimeException(e);
            }
            return getDistanceFunction().applyAsDouble(originalRobustnessVector, variantRobustnessVector);
        };
    }


    private record ModelSpecs(String module, String initialConfiguration, String modelSpecification){}

    private ToDoubleBiFunction<double[],double[]> getDistanceFunction(){
        switch (comparisonType) {
            case PRESERVE -> {
                this.isMinimization = true;
                return (vec1, vec2) -> distanceType.distanceFunction.apply(vec1, vec2, p, false);
            }
            case IMPROVE -> {
                this.isMinimization = false;
                return (vec1, vec2) -> distanceType.distanceFunction.apply(vec1, vec2, p, true);
            }
            case DIVERGE -> {
                this.isMinimization = false;
                return (vec1, vec2) -> distanceType.distanceFunction.apply(vec2, vec1, p, true);  }
            default -> throw new IllegalStateException("Unexpected value: " + comparisonType);
        }
    }

    private enum ComparisonType {
        PRESERVE, DIVERGE, IMPROVE;
        public static ComparisonType getValueOf(String name) {
            String normalizedInput = name.trim().toUpperCase();
            return ComparisonType.valueOf(normalizedInput);
        }
    }

    @Override
    public String toString() {
        return "ComparisonTask{" +
                "formulae='" + formulae + '\'' +
                ", modelVariantSpecs=" + modelVariantSpecs +
                ", modelOriginalSpecs=" + modelOriginalSpecs +
                ", comparisonType=" + comparisonType +
                ", distanceType=" + distanceType +
                ", p=" + p +
                '}';
    }

    private enum DistanceType {
        EUCLIDEAN((x,y,p,flag) -> minkowskiDistance(x,y,2,flag)),
        MANHATTAN((x,y,p,flag) -> minkowskiDistance(x,y,1,flag)),
        CHEBYSHEV((x,y,p,flag) -> minkowskiDistance(x,y,Double.POSITIVE_INFINITY,flag)),
        MINKOWSKI(DistanceType::minkowskiDistance);


        private final DistanceFunction distanceFunction;

         DistanceType(DistanceFunction distanceFunction) {
            this.distanceFunction = distanceFunction;
        }


        public static DistanceType getValueOf(String name) {
            String normalizedInput = name.trim().toLowerCase();
            return switch (normalizedInput) {
                case "euc", "euclidean" -> EUCLIDEAN;
                case "man", "manhattan" -> MANHATTAN;
                case "cheb", "chebyshev" -> CHEBYSHEV;
                case "min", "minkowski" -> MINKOWSKI;
                default -> throw new IllegalArgumentException("Invalid DistanceType: " + name);
            };
        }

        @FunctionalInterface
        private interface DistanceFunction {
            double apply(double[] point1, double[] point2,double p,boolean flag);
        }

        /**
         * Calculates the modified Minkowski distance between two vectors.
         *
         * @param x        First vector
         * @param y        Second vector
         * @param p        The order of the Minkowski distance
         * @param maximize If true, calculates distance for maximization; if false, for minimization
         * @return The modified Minkowski distance
         * @throws IllegalArgumentException If vectors have different lengths or p <= 0
         */
        public static double minkowskiDistance(double[] x, double[] y, double p, boolean maximize) {
            if (x.length != y.length) {
                throw new IllegalArgumentException("Vectors must have the same length");
            }
            if (p <= 0) {
                throw new IllegalArgumentException("p must be greater than 0");
            }

            double sum = 0;
            for (int i = 0; i < x.length; i++) {
                double diff = maximize ? Math.max(0, x[i] - y[i]) : Math.abs(x[i] - y[i]);
                sum += Math.pow(Math.abs(diff), p);
            }

            return Math.pow(sum, 1.0 / p);
        }

    }
}
