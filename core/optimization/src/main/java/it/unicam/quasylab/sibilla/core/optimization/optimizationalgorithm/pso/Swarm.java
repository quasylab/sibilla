package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * A class that represents a particle in
 * particle swarm optimization
 *
 * @author Lorenzo Matteucci
 */
public class Swarm {

    private final List<Particle> particles;
    private Particle globalBest;

    public Swarm(List<Particle> particles){
        this.particles = new ArrayList<>();
        this.particles.addAll(particles);
    }
    public Particle getGlobalBest() {
        return globalBest;
    }

    public List<Particle> getParticles(){
        return particles;
    }

    public void setGlobalBest(Particle globalBest) {
        this.globalBest = globalBest.getCopy();
    }
}