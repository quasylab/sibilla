package quasylab.sibilla.examples.benchmarks.seirslave;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.network.benchmark.computation.ComputationBenchmarkEnvironment;
import quasylab.sibilla.examples.pm.seir.SEIRModelDefinition;

public class ComputationBenchmark {

    public static void main(String[] args) {
        ModelDefinition modelDefinition = new SEIRModelDefinition();
        String benchmarkName = "comput4RulesSEIR";
        ComputationBenchmarkEnvironment computationBenchmark = ComputationBenchmarkEnvironment.
                getComputationEnvironment(ComputationBenchmarkEnvironment.Type.SEQUENTIAL, benchmarkName,
                        modelDefinition, 1, 900, 20);
        computationBenchmark.run();
    }
}
