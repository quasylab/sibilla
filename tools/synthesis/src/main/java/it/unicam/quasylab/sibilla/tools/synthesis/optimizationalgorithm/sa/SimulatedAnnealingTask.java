package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa.cooling.*;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.Interval;

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

    private CoolingSchedule coolingSchedule;

    public SimulatedAnnealingTask() {
        setProperties(new Properties());
    }

    public SimulatedAnnealingTask(Properties properties) {
        setProperties(properties);
    }
    private void performIteration(HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints) {
        Map<String, Double> currentSolution = generateInitialSolution(searchSpace, constraints);
        Map<String, Double> bestSolution = new HashMap<>(currentSolution);
        double currentTemperature = initialTemperature;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            Map<String, Double> newSolution = generateValidNeighborSolution(currentSolution, searchSpace, constraints);

            if (shouldAcceptNewSolution(currentSolution, newSolution, currentTemperature)) {
                currentSolution = newSolution;
            }

            updateBestSolution(currentSolution, bestSolution);
            currentTemperature =  coolingSchedule.cool(currentTemperature, iteration, maxIterations);
        }

        this.bestSolution = bestSolution;
    }

    private Map<String, Double> generateInitialSolution(HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints) {
        Map<String, Double> solution;
        do {
            solution = searchSpace.getRandomValue();
        } while (!isValidSolution(solution, constraints));
        return solution;
    }

    private Map<String, Double> generateValidNeighborSolution(Map<String, Double> currentSolution, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints) {
        Map<String, Double> newSolution;
        do {
            newSolution = generateNeighborSolution(currentSolution, searchSpace);
        } while (!isValidSolution(newSolution, constraints));
        return newSolution;
    }

    private boolean isValidSolution(Map<String, Double> solution, List<Predicate<Map<String, Double>>> constraints) {
        return constraints.stream().allMatch(c -> c.test(solution));
    }

    private boolean shouldAcceptNewSolution(Map<String, Double> currentSolution, Map<String, Double> newSolution, double currentTemperature) {
        double currentEnergy = fitnessFunction.evaluate(currentSolution);
        double newEnergy = fitnessFunction.evaluate(newSolution);
        return acceptanceProbability(currentEnergy, newEnergy, currentTemperature) > random.nextDouble();
    }

    private void updateBestSolution(Map<String, Double> currentSolution, Map<String, Double> bestSolution) {
        if (fitnessFunction.evaluate(currentSolution) < fitnessFunction.evaluate(bestSolution)) {
            bestSolution.clear();
            bestSolution.putAll(currentSolution);
        }
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
        String DEFAULT_COOLING_SCHEDULE = "linear";

        this.maxIterations = Integer.parseInt(properties.getProperty("sa.max_iterations", MAX_ITERATIONS + ""));
        this.initialTemperature = Double.parseDouble(properties.getProperty("sa.initial_temperature", INITIAL_TEMPERATURE + ""));
        this.coolingRate = Double.parseDouble(properties.getProperty("sa.cooling_rate", COOLING_RATE + ""));
        String coolingScheduleType = properties.getProperty("sa.cooling_schedule", DEFAULT_COOLING_SCHEDULE);
        switch (coolingScheduleType.toLowerCase()) {
            case "exponential":
                this.coolingSchedule = new ExponentialCoolingSchedule(coolingRate);
                break;
            case "logarithmic":
                this.coolingSchedule = new LogarithmicCoolingSchedule();
                break;
            case "linear":
            default:
                this.coolingSchedule = new LinearCoolingSchedule(coolingRate);
                break;
        }
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
