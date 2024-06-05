package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.sa;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class SimulatedAnnealingTask implements OptimizationTask {

    private int maxIterations;
    private double initialTemperature;
    private double coolingRate;
    private double penaltyValue;
    private FitnessFunction fitnessFunction;
    private Random random;

    public SimulatedAnnealingTask() {
        setProperties(new Properties());
    }

    public SimulatedAnnealingTask(Properties properties) {
        setProperties(properties);
    }

    private void performIteration(HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints) {
        Map<String, Double> currentSolution = searchSpace.getRandomValue();
        Map<String, Double> finalCurrentSolution = currentSolution;
        while (!constraints.stream().allMatch(c -> c.test(finalCurrentSolution))) {
            currentSolution = searchSpace.getRandomValue();
        }

        Map<String, Double> bestSolution = new HashMap<>(currentSolution);
        double currentTemperature = initialTemperature;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            Map<String, Double> newSolution = generateNeighborSolution(currentSolution, searchSpace);
            Map<String, Double> finalNewSolution = newSolution;
            while (!constraints.stream().allMatch(c -> c.test(finalNewSolution))) {
                newSolution = generateNeighborSolution(currentSolution, searchSpace);
            }

            double currentEnergy = fitnessFunction.evaluate(currentSolution);
            double newEnergy = fitnessFunction.evaluate(newSolution);

            if (acceptanceProbability(currentEnergy, newEnergy, currentTemperature) > random.nextDouble()) {
                currentSolution = newSolution;
            }

            if (fitnessFunction.evaluate(currentSolution) < fitnessFunction.evaluate(bestSolution)) {
                bestSolution = new HashMap<>(currentSolution);
            }

            currentTemperature *= (1 - coolingRate);
        }

        this.bestSolution = bestSolution;
    }

    private Map<String, Double> generateNeighborSolution(Map<String, Double> currentSolution, HyperRectangle searchSpace) {
        Map<String, Double> newSolution = new HashMap<>(currentSolution);
        List<String> keys = new ArrayList<>(currentSolution.keySet());
        String key = keys.get(random.nextInt(keys.size()));

        Interval interval = Arrays.stream(searchSpace.getIntervals())
                .filter(i -> i.getId().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Interval not found for key: " + key));

        double value = newSolution.get(key);
        value += (random.nextGaussian() * (interval.getUpperBound() - interval.getLowerBound()) * 0.1);
        value = Math.max(interval.getLowerBound(), Math.min(interval.getUpperBound(), value));
        newSolution.put(key, value);

        return newSolution;
    }

    private double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }

    @Override
    public Map<String, Double> minimize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints, Properties properties, Long seed) {
        constraints.addAll(getSearchSpaceAsConstraintList(searchSpace));
        setProperties(properties);
        this.random = new Random(seed);
        searchSpace.setSeeds(seed);
        this.penaltyValue = Double.POSITIVE_INFINITY;
        this.fitnessFunction = new FitnessFunction(objectiveFunction, constraints, penaltyValue);
        performIteration(searchSpace, constraints);
        return this.bestSolution;
    }

    @Override
    public void setProperties(Properties properties) {

        int MAX_ITERATIONS = 1000;
        double INITIAL_TEMPERATURE = 1000.0;
        double COOLING_RATE = 0.003;

        this.maxIterations = Integer.parseInt(properties.getProperty("sa.max_iterations", MAX_ITERATIONS + ""));
        this.initialTemperature = Double.parseDouble(properties.getProperty("sa.initial_temperature", INITIAL_TEMPERATURE + ""));
        this.coolingRate = Double.parseDouble(properties.getProperty("sa.cooling_rate", COOLING_RATE + ""));
    }

    private Map<String, Double> bestSolution;

    @Override
    public String toString() {
        String str = "\n Algorithm : Simulated Annealing ";
        str += "\n  - max iterations       : " + this.maxIterations;
        str += "\n  - initial temperature  : " + this.initialTemperature;
        str += "\n  - cooling rate         : " + this.coolingRate;
        return str;
    }

    private static class FitnessFunction {
        private final ToDoubleFunction<Map<String, Double>> objectiveFunction;
        private final List<Predicate<Map<String, Double>>> constraints;
        private final double penaltyValue;

        public FitnessFunction(ToDoubleFunction<Map<String, Double>> objectiveFunction, List<Predicate<Map<String, Double>>> constraints, double penaltyValue) {
            this.objectiveFunction = objectiveFunction;
            this.constraints = constraints;
            this.penaltyValue = penaltyValue;
        }

        public double evaluate(Map<String, Double> solution) {
            for (Predicate<Map<String, Double>> constraint : constraints) {
                if (!constraint.test(solution)) {
                    return penaltyValue;
                }
            }
            return objectiveFunction.applyAsDouble(solution);
        }
    }


}
