package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParticleSwarmOptimization implements OptimizationStrategy {

    private double inertia = 0.72984;
    private double selfConfidence = 1.49617;
    private double swarmConfidence = 1.49617;
    private final int numOfParticles;
    private double penaltyValue;
    private BiPredicate<Double,Double> evaluationCriteria;
    private final FitnessFunction fitnessFunction;
    private final HyperRectangle searchSpace;
    private final int iteration;
    private Swarm swarm;

    public ParticleSwarmOptimization(Function<Map<String,Double>,Double> functionToOptimize, HyperRectangle searchSpace, Properties properties){
        this(functionToOptimize,
                searchSpace,
                Integer.parseInt(properties.getProperty("iteration","1000")),
                Integer.parseInt(properties.getProperty("particlesNumber","1000")),
                Double.parseDouble(properties.getProperty("inertia","0.72984")),
                Double.parseDouble(properties.getProperty("selfConfidence","1.49617")),
                Double.parseDouble(properties.getProperty("swarmConfidence","1.49617"))
        );
    }
    public ParticleSwarmOptimization(Function<Map<String,Double>,Double> functionToOptimize, List<Predicate<Map<String,Double>>> constraints, HyperRectangle searchSpace, Properties properties){
        this(functionToOptimize,
                constraints,
                searchSpace,
                Integer.parseInt(properties.getProperty("iteration","1000")),
                Integer.parseInt(properties.getProperty("particlesNumber","1000")),
                Double.parseDouble(properties.getProperty("inertia","0.72984")),
                Double.parseDouble(properties.getProperty("selfConfidence","1.49617")),
                Double.parseDouble(properties.getProperty("swarmConfidence","1.49617"))
        );
    }
    public ParticleSwarmOptimization(Function<Map<String, Double>, Double> functionToOptimize, List<Predicate<Map<String, Double>>> constraints, HyperRectangle searchSpace, int iteration, int particlesNumber, double inertia, double selfConfidence, double swarmConfidence) {
        this.fitnessFunction = new FitnessFunction(functionToOptimize,constraints);
        this.searchSpace = searchSpace;
        this.iteration = iteration;
        this.numOfParticles = particlesNumber;
        this.inertia = inertia;
        this.selfConfidence = selfConfidence;
        this.swarmConfidence = swarmConfidence;
    }
    public ParticleSwarmOptimization(
            Function<Map<String,Double>,Double> fitnessFunction,
            HyperRectangle searchSpace,
            int iteration,
            int numOfParticles){
        this.fitnessFunction = new FitnessFunction(fitnessFunction);
        this.searchSpace = searchSpace;
        this.iteration = iteration;
        this.numOfParticles = numOfParticles;
    }
    public ParticleSwarmOptimization(
            Function<Map<String,Double>,Double> fitnessFunction,
            List<Predicate<Map<String,Double>>> constraints,
            HyperRectangle searchSpace,
            int iteration,
            int numOfParticles){
        this.fitnessFunction = new FitnessFunction(fitnessFunction,constraints);
        this.searchSpace = searchSpace;
        this.iteration = iteration;
        this.numOfParticles = numOfParticles;
    }
    public ParticleSwarmOptimization(
            Function<Map<String, Double>, Double> functionToOptimize,
            HyperRectangle searchSpace,
            int iteration,
            int particlesNumber,
            double inertia,
            double selfConfidence,
            double swarmConfidence) {
        this.fitnessFunction = new FitnessFunction(functionToOptimize);
        this.searchSpace = searchSpace;
        this.iteration = iteration;
        this.numOfParticles = particlesNumber;
        this.inertia = inertia;
        this.selfConfidence = selfConfidence;
        this.swarmConfidence = swarmConfidence;

    }


    public Map<String,Double> maximize(){
        this.penaltyValue = Double.MIN_VALUE;
        this.evaluationCriteria = (x, y) -> x < y;
        this.swarm = generateSwarm();
        performIteration();
        return swarm.getGBest();
    }

    public Map<String,Double> minimize(){
        this.penaltyValue = Double.MAX_VALUE;
        this.evaluationCriteria = (x, y) -> x > y;
        this.swarm = generateSwarm();
        performIteration();
        return swarm.getGBest();
    }

    private Swarm generateSwarm(){
        fitnessFunction.setPenaltyValue(this.penaltyValue);
        return new Swarm(this.numOfParticles,this.searchSpace,this.fitnessFunction,this.penaltyValue,this.evaluationCriteria);
    }

    //TODO : Could be parallelized, gBest should be synchronized
    private void performIteration(){
        for (int i = 0; i < iteration; i++) {
            for (Particle particle : this.swarm.getParticles()) {
                updateVelocityOf(particle);
                updatePositionOf(particle);
                double currentFitness = fitnessFunction.evaluate(particle.getPosition());
                boolean currentBetterThanPBest = evaluationCriteria.test(fitnessFunction.evaluate(particle.getPBest()),currentFitness);
                boolean currentBetterThanGBest = evaluationCriteria.test(fitnessFunction.evaluate(swarm.getGBest()),currentFitness);
                if(currentBetterThanPBest)
                    particle.setPBest(particle.getPosition());
                if(currentBetterThanGBest)
                    swarm.setGBest(particle.getPosition());
            }
        }
    }

    private void updateVelocityOf(Particle particle){
        Random r = new Random();
        for (String valueName: particle.getVelocity().keySet()) {
            double newValue = inertia * particle.getVelocity().get(valueName) +
                    r.nextDouble() * selfConfidence * ( particle.getPBest().get(valueName) - particle.getPosition().get(valueName)) +
                    r.nextDouble() * swarmConfidence * (swarm.getGBest().get(valueName) - particle.getPosition().get(valueName));
            particle.getVelocity().put(valueName, newValue);
        }
    }

    private void updatePositionOf(Particle particle){
        for (String valueName: particle.getPosition().keySet()) {
            double currentValue = particle.getPosition().get(valueName);
            particle.getPosition().put(valueName,currentValue + particle.getVelocity().get(valueName));
        }
    }

    public void setSearchSpaceAsConstraints(){
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
        for (Interval i :searchSpace.getIntervals()) {
            constraints.add( map -> i.getLowerBound() <= map.get(i.getId()) && map.get(i.getId()) <= i.getUpperBound() );
        }
        this.fitnessFunction.addConstraints(constraints);
    }

}
