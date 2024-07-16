package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.pso;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.Interval;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;


public class PSOTask implements OptimizationTask {
    private double inertia;
    private double selfConfidence;
    private double swarmConfidence;
    private int numberOfParticles;
    private int iteration;
    private double penaltyValue;
    private BiPredicate<Double,Double> evaluationCriteria;
    private FitnessFunction fitnessFunction;
    private Swarm swarm;
    private Random random;

    public PSOTask(){
        setProperties(new Properties());
    }

    public PSOTask(Properties properties){
        setProperties(properties);
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

    private Swarm getPopulatedSwarm(HyperRectangle searchSpace){
        Swarm newSwarm = new Swarm(getParticleList(searchSpace));
        Particle gBestParticle = newSwarm.getParticles().get(0);
        for (Particle p :newSwarm.getParticles()) {
            if(!this.evaluationCriteria.test(gBestParticle.getFitness(),p.getFitness()))
                gBestParticle = p;
        }
        newSwarm.setGlobalBest(gBestParticle);
        return newSwarm;
    }


    private List<Particle> getParticleList(HyperRectangle searchSpace){
        ArrayList<Particle> particles = new ArrayList<>();

        for (int i = 0; i < this.numberOfParticles; i++) {
            Map<String,Double> position = new HashMap<>();
            Map<String,Double> velocity = new HashMap<>();
            for (Interval interval : searchSpace.getIntervals()) {
                position.put(interval.getId(), interval.getRandomValue());
                velocity.put(interval.getId(), interval.getRandomValue() * 0.25);
            }
            Particle p = new Particle(position,velocity);
            p.setFitness(this.fitnessFunction.evaluate(position));
            particles.add(p);
        }
        particles.forEach(particle -> particle.setParticleBest(particles.get(random.nextInt(particles.size()))));

        return particles;
    }

    private void updateVelocityOf(Particle particle){
        for (String valueName: particle.getVelocity().keySet()) {
            double newValue = inertia * particle.getVelocity().get(valueName) +
                    random.nextDouble() * selfConfidence * ( particle.getParticleBest().getPosition().get(valueName) - particle.getPosition().get(valueName)) +
                    random.nextDouble() * swarmConfidence * (swarm.getGlobalBest().getPosition().get(valueName) - particle.getPosition().get(valueName));
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

    @Override
    public Map<String, Double> minimize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints,Properties properties, Long seed) {
        constraints.addAll(getSearchSpaceAsConstraintList(searchSpace));
        setProperties(properties);
        this.random = new Random(seed);
        searchSpace.setSeeds(seed);
        this.penaltyValue = Double.POSITIVE_INFINITY;
        this.evaluationCriteria = (x, y) -> x < y;
        this.fitnessFunction = new FitnessFunction(objectiveFunction, constraints, penaltyValue);
        this.swarm = getPopulatedSwarm(searchSpace);
        performIteration();
        return this.swarm.getGlobalBest().getPosition();
    }


    @Override
    public void setProperties(Properties properties) {

        double INERTIA = 0.72984;
        double SELF_CONFIDENCE = 1.49617;
        double SWARM_CONFIDENCE = 1.49617;
        int NUMBER_OF_PARTICLES = 100;
        int ITERATION = 500;

        this.inertia = Double.parseDouble(properties.getProperty("pso.inertia", INERTIA +""));
        this.selfConfidence = Double.parseDouble(properties.getProperty("pso.self_confidence", SELF_CONFIDENCE +""));
        this.swarmConfidence = Double.parseDouble(properties.getProperty("pso.swarm_confidence", SWARM_CONFIDENCE +""));
        this.numberOfParticles = Integer.parseInt(properties.getProperty("pso.particles_number", NUMBER_OF_PARTICLES +""));
        this.iteration = Integer.parseInt(properties.getProperty("pso.iteration", ITERATION +""));

    }

    @Override
    public String toString() {
        String str = "\n Algorithm : Particle Swarm Optimization ";
        str += "\n  - inertia              : "+this.inertia;
        str += "\n  - self confidence      : "+this.selfConfidence;
        str += "\n  - swarm confidence     : "+this.swarmConfidence;
        str += "\n  - number of particles  : "+this.numberOfParticles;
        str += "\n  - iteration            : "+this.iteration;
        return str;
    }
}
