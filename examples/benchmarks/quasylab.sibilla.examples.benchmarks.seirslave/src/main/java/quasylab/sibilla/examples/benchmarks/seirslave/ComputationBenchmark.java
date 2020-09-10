package quasylab.sibilla.examples.benchmarks.seirslave;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.network.benchmark.computation.ComputationBenchmarkEnvironment;

import java.io.IOException;

public class ComputationBenchmark {

    public static void main(String[] args) {
        ModelDefinition modelDefinition = new SEIRModelDefinitionFourRules();
        ComputationBenchmarkEnvironment computationBenchmark = ComputationBenchmarkEnvironment.
                getComputationEnvironment(modelDefinition);
        computationBenchmark.run();
    }
}
