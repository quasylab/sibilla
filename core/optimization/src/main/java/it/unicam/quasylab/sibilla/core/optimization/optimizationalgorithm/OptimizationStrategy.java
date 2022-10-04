package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;
import java.util.Map;

/**
 *
 * a class that represent an implementation of the
 * OptimizationStrategy.
 * Particle Swarm Optimization (PSO) is a powerful meta-heuristic
 * optimization algorithm and inspired by swarm behavior observed
 * in nature such as fish and bird schooling.
 * PSO is a Simulation of a simplified social system.
 *
 * @author Lorenzo Matteucci
 */
public interface OptimizationStrategy {
    /**
     * Evaluate a vector of parameters that minimizes
     * the result of the objective function
     *
     * @return a vector of parameters that minimizes
     * the result of the objective function, the vector
     * is expressed as a map that associates the name
     * of a certain variable in the vector with its value
     */
    Map<String,Double> minimize();
    /**
     * Evaluate a vector of parameters that maximizes
     * the result of the objective function
     *
     * @return a vector of parameters that maximizes
     * the result of the objective function, the vector
     * is expressed as a map that associates the name
     * of a certain variable in the vector with its value
     */
    Map<String,Double> maximize();

    /**
     * method that allows you to set the search space as constraints
     */
    void setSearchSpaceAsConstraints();
}
