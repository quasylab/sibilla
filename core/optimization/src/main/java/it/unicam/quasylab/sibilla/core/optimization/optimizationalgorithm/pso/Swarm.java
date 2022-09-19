package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class Swarm {

    private final Particle[] particles;
    private Map<String,Double> gBest;
    private final FitnessFunction fitnessFunction;
    private final BiPredicate<Double,Double> evaluationCriteria;
    private double gBestFitness;


    public Swarm(int numberParticles, HyperRectangle searchSpace, FitnessFunction fitnessFunction, double initialBestFitness, BiPredicate<Double,Double> evaluationCriteria){
        this.particles = new Particle[numberParticles];
        this.fitnessFunction = fitnessFunction;
        this.gBestFitness = initialBestFitness;
        this.evaluationCriteria = evaluationCriteria;
        for (int i = 0; i < numberParticles; i++) {
            particles[i] = getParticleFromHyperRectangle(searchSpace);
        }
    }

    public Particle[] getParticles() {
        return particles;
    }

    private Particle getParticleFromHyperRectangle(HyperRectangle searchSpace) {
        Map<String,Double> positionMap = new HashMap<>();
        Map<String,Double> velocityMap = new HashMap<>();
        for (Interval i : searchSpace.getIntervals()) {
            positionMap.put(i.getId(), i.getRandomValue());
            velocityMap.put(i.getId(), i.getRandomValue() * 0.25);
        }

        double particleFitness = this.fitnessFunction.evaluate(positionMap);

        if(evaluationCriteria.test(gBestFitness,particleFitness) || gBest == null){
            gBestFitness = particleFitness;
            gBest = positionMap;
        }

        return new Particle(positionMap,velocityMap,positionMap);
    }

    public Map<String, Double> getGBest() {
        return gBest;
    }

    public void setGBest(Map<String, Double> gBest) {
        this.gBest = gBest;
    }
}
