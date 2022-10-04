package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
/**
 *
 * A class that represents a particle in
 * particle swarm optimization
 *
 * @author Lorenzo Matteucci
 */
public class ParticleSwarmOptimization implements OptimizationStrategy {
    private final Function<Map<String,Double>,Double> functionToOptimize;
    private final List<Predicate<Map<String,Double>>> constraints;
    private final HyperRectangle searchSpace;
    private final double inertia;
    private final double selfConfidence;
    private final double swarmConfidence;
    private final int numberOfParticles;
    private final int iteration;
    private double penaltyValue;
    private BiPredicate<Double,Double> evaluationCriteria;
    private FitnessFunction fitnessFunction;
    private Swarm swarm;

    public ParticleSwarmOptimization(Function<Map<String,Double>,Double> functionToOptimize,
                                     List<Predicate<Map<String,Double>>> constraints,
                                     HyperRectangle searchSpace,
                                     Properties properties){

        this.functionToOptimize = functionToOptimize;
        this.constraints = Optional.ofNullable(constraints).orElse(new ArrayList<>());
        this.searchSpace = searchSpace;

        this.inertia = Double.parseDouble(properties.getProperty("pso.inertia","0.72984"));
        this.selfConfidence = Double.parseDouble(properties.getProperty("pso.self.confidence","1.49617"));
        this.swarmConfidence = Double.parseDouble(properties.getProperty("pso.swarm.confidence","1.49617"));

        this.numberOfParticles = Integer.parseInt(properties.getProperty("pso.particles.number","100"));
        this.iteration = Integer.parseInt(properties.getProperty("pso.iteration","100"));
    }
    @Override
    public Map<String, Double> minimize() {
        this.penaltyValue = Double.POSITIVE_INFINITY;
        this.evaluationCriteria = (x, y) -> x < y;
        this.fitnessFunction = new FitnessFunction(this.functionToOptimize, this.constraints, this.penaltyValue);
        this.swarm = getPopulatedSwarm();
        performIteration();
        return this.swarm.getGlobalBest().getPosition();
    }

    @Override
    public Map<String, Double> maximize() {
        this.penaltyValue = Double.NEGATIVE_INFINITY;
        this.evaluationCriteria = (x, y) -> x > y;
        this.fitnessFunction = new FitnessFunction(this.functionToOptimize, this.constraints, this.penaltyValue);
        this.swarm = getPopulatedSwarm();
        performIteration();
        return this.swarm.getGlobalBest().getPosition();
    }

    public void setSearchSpaceAsConstraints(){
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
        for (Interval i :searchSpace.getIntervals()) {
            constraints.add( map -> i.getLowerBound() <= map.get(i.getId()) && map.get(i.getId()) <= i.getUpperBound() );
        }
        this.constraints.addAll(constraints);
    }

    private void performIteration(){
        for (int i = 0; i < iteration; i++) {
            for (Particle particle : this.swarm.getParticles()) {
                updateVelocityOf(particle);
                updatePositionOf(particle);
                double currentFitness = particle.getFitness();
                double pBest = particle.getParticleBest().getFitness();
                double gBest = swarm.getGlobalBest().getFitness();
                boolean currentBetterThanPBest = evaluationCriteria.test(currentFitness,pBest);
                boolean currentBetterThanGBest = evaluationCriteria.test(currentFitness,gBest);
                if(currentBetterThanPBest)
                    particle.setParticleBest(particle);
                if(currentBetterThanGBest)
                    swarm.setGlobalBest(particle);
            }
        }

    }

    private Swarm getPopulatedSwarm(){
        Swarm newSwarm = new Swarm(getParticleList());
        Particle gBestParticle = newSwarm.getParticles().get(0);
        for (Particle p :newSwarm.getParticles()) {
            if(!this.evaluationCriteria.test(gBestParticle.getFitness(),p.getFitness()))
                gBestParticle = p;
        }
        newSwarm.setGlobalBest(gBestParticle);
        return newSwarm;
    }


    private List<Particle> getParticleList(){
        ArrayList<Particle> particles = new ArrayList<>();

        for (int i = 0; i < this.numberOfParticles; i++) {
            Map<String,Double> position = new HashMap<>();
            Map<String,Double> velocity = new HashMap<>();
            for (Interval interval : this.searchSpace.getIntervals()) {
                position.put(interval.getId(), interval.getRandomValue());
                velocity.put(interval.getId(), interval.getRandomValue() * 0.25);
            }
            Particle p = new Particle(position,velocity);
            p.setFitness(this.fitnessFunction.evaluate(position));
            particles.add(p);
        }

        Random rand = new Random();
        particles.forEach(particle -> particle.setParticleBest(particles.get(rand.nextInt(particles.size()))));

        return particles;
    }

    private void updateVelocityOf(Particle particle){
        Random r = new Random();
        for (String valueName: particle.getVelocity().keySet()) {
            double newValue = inertia * particle.getVelocity().get(valueName) +
                    r.nextDouble() * selfConfidence * ( particle.getParticleBest().getPosition().get(valueName) - particle.getPosition().get(valueName)) +
                    r.nextDouble() * swarmConfidence * (swarm.getGlobalBest().getPosition().get(valueName) - particle.getPosition().get(valueName));
            particle.getVelocity().put(valueName, newValue);
        }
    }

    private void updatePositionOf(Particle particle){
        for (String valueName: particle.getPosition().keySet()) {
            double currentValue = particle.getPosition().get(valueName);
            particle.getPosition().put(valueName,currentValue + particle.getVelocity().get(valueName));
        }
        particle.setFitness(this.fitnessFunction.evaluate(particle.getPosition()));
    }
}
