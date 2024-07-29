package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.pso;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationAlgorithm;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;

public class PSOAlgorithm implements OptimizationAlgorithm {
    @Override
    public OptimizationTask getOptimizationTask() {
        return new PSOTask();
    }




//        this.inertia = Double.parseDouble(properties.getProperty("pso.inertia", INERTIA +""));
//        this.selfConfidence = Double.parseDouble(properties.getProperty("pso.self_confidence", SELF_CONFIDENCE +""));
//        this.swarmConfidence = Double.parseDouble(properties.getProperty("pso.swarm_confidence", SWARM_CONFIDENCE +""));
//        this.numberOfParticles = Integer.parseInt(properties.getProperty("pso.particles_number", NUMBER_OF_PARTICLES +""));
//        this.iteration = Integer.parseInt(properties.getProperty("pso.iteration", ITERATION +""));
}
