package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import java.util.Map;
import java.util.stream.Collectors;
/**
 *
 * A class that represents a particle in
 * particle swarm optimization
 *
 * @author Lorenzo Matteucci
 */
public class Particle {

    private final Map<String,Double> position;
    private final Map<String,Double> velocity;
    private double fitness;
    private Particle particleBest;

    Particle(Map<String,Double> position,Map<String,Double> velocity){
        this.position = position;
        this.velocity = velocity;
    }

    public Map<String, Double> getPosition() {
        return position;
    }

    public Map<String, Double> getVelocity() {
        return velocity;
    }

    public Particle getParticleBest() {
        return particleBest;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public void setParticleBest(Particle particleBest) {
        this.particleBest = particleBest.getCopy();
    }
    public Particle getCopy(){
        Particle particle = new Particle(getMapCopy(this.position),getMapCopy(this.velocity));
        particle.setFitness(this.fitness);
        return particle;
    }

    private Map<String,Double> getMapCopy(Map<String,Double> map){
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}