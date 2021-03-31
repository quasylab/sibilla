package it.unicam.quasylab.sibilla.examples.benchmarks.slave;


import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.network.benchmark.computation.ComputationBenchmarkEnvironment;
import it.unicam.quasylab.sibilla.examples.pm.seir.SEIRModelDefinition;

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
